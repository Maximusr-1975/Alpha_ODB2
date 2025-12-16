#!/usr/bin/env python3
"""
Google Photos Sync - Synchronizacja zdjęć z Google Photos
Program do automatycznej synchronizacji zdjęć z Google Photos na lokalny dysk
"""

import os
import sys
import json
import pickle
import argparse
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Optional

try:
    from google.auth.transport.requests import Request
    from google.oauth2.credentials import Credentials
    from google_auth_oauthlib.flow import InstalledAppFlow
    from googleapiclient.discovery import build
    from googleapiclient.http import MediaIoBaseDownload
    import requests
except ImportError:
    print("Błąd: Brakujące zależności. Uruchom: pip install -r requirements.txt")
    sys.exit(1)

# Uprawnienia wymagane do odczytu Google Photos
SCOPES = ['https://www.googleapis.com/auth/photoslibrary.readonly']

class GooglePhotosSync:
    """Klasa do synchronizacji zdjęć z Google Photos"""

    def __init__(self, download_dir: str, credentials_file: str = 'credentials.json'):
        """
        Inicjalizacja synchronizatora

        Args:
            download_dir: Katalog docelowy dla pobranych zdjęć
            credentials_file: Plik z danymi uwierzytelniającymi Google API
        """
        self.download_dir = Path(download_dir)
        self.credentials_file = credentials_file
        self.token_file = 'token.pickle'
        self.sync_state_file = self.download_dir / '.sync_state.json'
        self.service = None
        self.sync_state = self._load_sync_state()

        # Utwórz katalog jeśli nie istnieje
        self.download_dir.mkdir(parents=True, exist_ok=True)

    def _load_sync_state(self) -> Dict:
        """Wczytaj stan synchronizacji (które pliki już pobrano)"""
        if self.sync_state_file.exists():
            with open(self.sync_state_file, 'r') as f:
                return json.load(f)
        return {'downloaded_files': {}, 'last_sync': None}

    def _save_sync_state(self):
        """Zapisz stan synchronizacji"""
        self.sync_state['last_sync'] = datetime.now().isoformat()
        with open(self.sync_state_file, 'w') as f:
            json.dump(self.sync_state, f, indent=2)

    def authenticate(self) -> bool:
        """
        Uwierzytelnianie z Google Photos API

        Returns:
            True jeśli uwierzytelnianie powiodło się
        """
        creds = None

        # Sprawdź czy istnieje zapisany token
        if os.path.exists(self.token_file):
            with open(self.token_file, 'rb') as token:
                creds = pickle.load(token)

        # Jeśli brak ważnych danych logowania, zaloguj użytkownika
        if not creds or not creds.valid:
            if creds and creds.expired and creds.refresh_token:
                print("Odświeżanie tokenu dostępu...")
                creds.refresh(Request())
            else:
                if not os.path.exists(self.credentials_file):
                    print(f"Błąd: Nie znaleziono pliku {self.credentials_file}")
                    print("Pobierz plik credentials.json z Google Cloud Console")
                    return False

                print("Logowanie do Google Photos...")
                flow = InstalledAppFlow.from_client_secrets_file(
                    self.credentials_file, SCOPES)
                creds = flow.run_local_server(port=0)

            # Zapisz token dla przyszłych użyć
            with open(self.token_file, 'wb') as token:
                pickle.dump(creds, token)

        # Utwórz serwis API
        self.service = build('photoslibrary', 'v1', credentials=creds, static_discovery=False)
        print("✓ Uwierzytelniono pomyślnie")
        return True

    def get_all_media_items(self) -> List[Dict]:
        """
        Pobierz listę wszystkich zdjęć i filmów z Google Photos

        Returns:
            Lista elementów multimedialnych
        """
        print("Pobieranie listy zdjęć...")
        items = []
        page_token = None

        while True:
            try:
                results = self.service.mediaItems().list(
                    pageSize=100,
                    pageToken=page_token
                ).execute()

                batch = results.get('mediaItems', [])
                items.extend(batch)

                page_token = results.get('nextPageToken')
                print(f"  Pobrano informacje o {len(items)} elementach...", end='\r')

                if not page_token:
                    break

            except Exception as e:
                print(f"\nBłąd podczas pobierania listy: {e}")
                break

        print(f"\n✓ Znaleziono {len(items)} elementów")
        return items

    def download_media_item(self, item: Dict) -> bool:
        """
        Pobierz pojedynczy element multimedialny

        Args:
            item: Słownik z informacjami o elemencie

        Returns:
            True jeśli pobrano pomyślnie
        """
        item_id = item['id']
        filename = item['filename']

        # Sprawdź czy już pobrano
        if item_id in self.sync_state['downloaded_files']:
            return True

        try:
            # Przygotuj URL do pobrania
            base_url = item['baseUrl']

            # Dodaj parametry dla pełnej jakości
            if 'video' in item.get('mimeType', ''):
                download_url = f"{base_url}=dv"
            else:
                download_url = f"{base_url}=d"

            # Organizuj pliki po dacie utworzenia
            creation_time = item.get('mediaMetadata', {}).get('creationTime', '')
            if creation_time:
                date = datetime.fromisoformat(creation_time.replace('Z', '+00:00'))
                year_month_dir = self.download_dir / date.strftime('%Y/%Y-%m')
            else:
                year_month_dir = self.download_dir / 'unknown'

            year_month_dir.mkdir(parents=True, exist_ok=True)

            # Ścieżka docelowa
            file_path = year_month_dir / filename

            # Pobierz plik
            response = requests.get(download_url, stream=True)
            response.raise_for_status()

            with open(file_path, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    f.write(chunk)

            # Zapisz informację o pobranym pliku
            self.sync_state['downloaded_files'][item_id] = {
                'filename': filename,
                'path': str(file_path),
                'downloaded_at': datetime.now().isoformat()
            }

            return True

        except Exception as e:
            print(f"\n  Błąd podczas pobierania {filename}: {e}")
            return False

    def sync(self, max_items: Optional[int] = None):
        """
        Synchronizuj wszystkie zdjęcia

        Args:
            max_items: Maksymalna liczba elementów do pobrania (None = wszystkie)
        """
        if not self.authenticate():
            return

        # Pobierz listę elementów
        items = self.get_all_media_items()

        if not items:
            print("Nie znaleziono żadnych zdjęć do synchronizacji")
            return

        # Ogranicz liczbę elementów jeśli podano
        if max_items:
            items = items[:max_items]

        # Filtruj tylko nowe elementy
        new_items = [item for item in items
                     if item['id'] not in self.sync_state['downloaded_files']]

        if not new_items:
            print("✓ Wszystkie zdjęcia są już zsynchronizowane")
            return

        print(f"\nPobieranie {len(new_items)} nowych elementów...")

        success_count = 0
        for i, item in enumerate(new_items, 1):
            filename = item['filename']
            print(f"[{i}/{len(new_items)}] Pobieranie: {filename}...", end='\r')

            if self.download_media_item(item):
                success_count += 1

                # Zapisuj stan co 10 plików
                if success_count % 10 == 0:
                    self._save_sync_state()

        # Zapisz końcowy stan
        self._save_sync_state()

        print(f"\n\n✓ Synchronizacja zakończona!")
        print(f"  Pobrano: {success_count} nowych plików")
        print(f"  Całkowita liczba zsynchronizowanych plików: {len(self.sync_state['downloaded_files'])}")
        print(f"  Katalog: {self.download_dir}")

    def get_stats(self):
        """Wyświetl statystyki synchronizacji"""
        print("\n=== Statystyki synchronizacji ===")
        print(f"Katalog docelowy: {self.download_dir}")
        print(f"Liczba zsynchronizowanych plików: {len(self.sync_state['downloaded_files'])}")

        if self.sync_state['last_sync']:
            last_sync = datetime.fromisoformat(self.sync_state['last_sync'])
            print(f"Ostatnia synchronizacja: {last_sync.strftime('%Y-%m-%d %H:%M:%S')}")
        else:
            print("Ostatnia synchronizacja: Nigdy")
        print()


def main():
    """Główna funkcja programu"""
    parser = argparse.ArgumentParser(
        description='Synchronizacja zdjęć z Google Photos',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Przykłady użycia:
  %(prog)s --sync                    # Synchronizuj wszystkie zdjęcia
  %(prog)s --sync --max 100         # Synchronizuj maksymalnie 100 zdjęć
  %(prog)s --stats                   # Wyświetl statystyki
  %(prog)s --dir /mnt/photos --sync  # Użyj innego katalogu
        """
    )

    parser.add_argument('--dir', '-d',
                       default='./google_photos',
                       help='Katalog docelowy dla zdjęć (domyślnie: ./google_photos)')

    parser.add_argument('--credentials', '-c',
                       default='credentials.json',
                       help='Plik z danymi uwierzytelniającymi (domyślnie: credentials.json)')

    parser.add_argument('--sync', '-s',
                       action='store_true',
                       help='Wykonaj synchronizację')

    parser.add_argument('--max', '-m',
                       type=int,
                       help='Maksymalna liczba elementów do pobrania')

    parser.add_argument('--stats',
                       action='store_true',
                       help='Wyświetl statystyki synchronizacji')

    args = parser.parse_args()

    # Utwórz instancję synchronizatora
    syncer = GooglePhotosSync(args.dir, args.credentials)

    if args.stats:
        syncer.get_stats()
    elif args.sync:
        syncer.sync(max_items=args.max)
    else:
        parser.print_help()


if __name__ == '__main__':
    main()
