#!/usr/bin/env python3
"""
Skrypt diagnostyczny dla Google Photos API
"""

import os
import sys
import pickle

try:
    from google.auth.transport.requests import Request
    from google.oauth2.credentials import Credentials
    from google_auth_oauthlib.flow import InstalledAppFlow
    from googleapiclient.discovery import build
    from googleapiclient.errors import HttpError
except ImportError:
    print("❌ Błąd: Brakujące zależności. Uruchom: pip install -r requirements.txt")
    sys.exit(1)

SCOPES = ['https://www.googleapis.com/auth/photoslibrary.readonly']

def test_authentication():
    """Test uwierzytelniania"""
    print("\n" + "="*60)
    print("TEST 1: Uwierzytelnianie")
    print("="*60)

    creds = None

    if os.path.exists('token.pickle'):
        print("✓ Znaleziono token.pickle")
        with open('token.pickle', 'rb') as token:
            creds = pickle.load(token)
            print(f"  - Token valid: {creds.valid if creds else 'N/A'}")
            print(f"  - Token expired: {creds.expired if creds else 'N/A'}")
            if creds:
                print(f"  - Scopes: {creds.scopes if hasattr(creds, 'scopes') else 'N/A'}")
    else:
        print("❌ Brak pliku token.pickle")
        return None

    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            print("⚠ Token wygasł, odświeżanie...")
            try:
                creds.refresh(Request())
                print("✓ Token odświeżony pomyślnie")
            except Exception as e:
                print(f"❌ Błąd odświeżania tokenu: {e}")
                return None
        else:
            print("❌ Token nieprawidłowy, wymagane ponowne logowanie")
            if not os.path.exists('credentials.json'):
                print("❌ Brak pliku credentials.json")
                return None

            try:
                flow = InstalledAppFlow.from_client_secrets_file('credentials.json', SCOPES)
                creds = flow.run_local_server(port=0)
                with open('token.pickle', 'wb') as token:
                    pickle.dump(creds, token)
                print("✓ Nowy token utworzony")
            except Exception as e:
                print(f"❌ Błąd podczas logowania: {e}")
                return None

    return creds

def test_api_access(creds):
    """Test dostępu do API"""
    print("\n" + "="*60)
    print("TEST 2: Dostęp do Photos Library API")
    print("="*60)

    try:
        service = build('photoslibrary', 'v1', credentials=creds, static_discovery=False)
        print("✓ Połączono z Photos Library API")
        return service
    except Exception as e:
        print(f"❌ Błąd budowania serwisu: {e}")
        return None

def test_list_media(service):
    """Test pobierania listy zdjęć"""
    print("\n" + "="*60)
    print("TEST 3: Pobieranie listy zdjęć")
    print("="*60)

    try:
        print("Wysyłanie zapytania do API...")
        results = service.mediaItems().list(pageSize=10).execute()

        items = results.get('mediaItems', [])
        print(f"✓ Zapytanie wykonane pomyślnie")
        print(f"  - Liczba pobranych elementów: {len(items)}")

        if items:
            print(f"\n📸 Przykładowe zdjęcia:")
            for i, item in enumerate(items[:3], 1):
                print(f"  {i}. {item.get('filename', 'N/A')}")
                print(f"     ID: {item.get('id', 'N/A')[:20]}...")
                print(f"     Typ: {item.get('mimeType', 'N/A')}")
        else:
            print("\n⚠ Nie znaleziono żadnych zdjęć w Google Photos")
            print("   Sprawdź czy Twoje konto Google Photos ma jakieś zdjęcia:")
            print("   https://photos.google.com/")

        return True

    except HttpError as e:
        print(f"❌ Błąd HTTP: {e}")
        print(f"\n📋 Szczegóły błędu:")
        print(f"  - Status code: {e.resp.status}")
        print(f"  - Reason: {e.resp.reason}")
        print(f"  - Error details: {e.error_details}")

        if e.resp.status == 403:
            print("\n💡 Rozwiązanie dla błędu 403:")
            print("  1. Usuń plik token.pickle")
            print("  2. Sprawdź scopes w Google Cloud Console:")
            print("     https://console.cloud.google.com/apis/credentials/consent")
            print("  3. Upewnij się że scope '.../auth/photoslibrary.readonly' jest dodany")
            print("  4. Zaloguj się ponownie")

        elif e.resp.status == 401:
            print("\n💡 Rozwiązanie dla błędu 401:")
            print("  1. Włącz Photos Library API:")
            print("     https://console.cloud.google.com/apis/library/photoslibrary.googleapis.com")
            print("  2. Usuń token.pickle i zaloguj się ponownie")

        return False

    except Exception as e:
        print(f"❌ Nieoczekiwany błąd: {e}")
        print(f"   Typ błędu: {type(e).__name__}")
        return False

def main():
    print("\n🔍 DIAGNOSTYKA GOOGLE PHOTOS API")
    print("="*60)

    # Test 1: Uwierzytelnianie
    creds = test_authentication()
    if not creds:
        print("\n❌ Test uwierzytelniania nie powiódł się")
        return

    # Test 2: Budowanie serwisu API
    service = test_api_access(creds)
    if not service:
        print("\n❌ Test dostępu do API nie powiódł się")
        return

    # Test 3: Pobieranie zdjęć
    success = test_list_media(service)

    print("\n" + "="*60)
    if success:
        print("✅ WSZYSTKIE TESTY ZAKOŃCZONE POMYŚLNIE")
        print("\nProgram powinien działać poprawnie!")
        print("Uruchom: python google_photos_sync.py --dir \"G:\\GOOGLE_IMAGES\" --sync")
    else:
        print("❌ TESTY NIE POWIODŁY SIĘ")
        print("\nPostępuj zgodnie z instrukcjami powyżej")
    print("="*60 + "\n")

if __name__ == '__main__':
    main()
