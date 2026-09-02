import os
from dotenv import load_dotenv
from msal import PublicClientApplication

# Load environment variables from .env file
load_dotenv()

# Get the client ID and tenant ID from environment variables
client_id = os.getenv("VITE_AZURE_CLIENT_ID")
tenant_id = os.getenv("VITE_AZURE_TENANT_ID")

if not client_id or not tenant_id:
    raise ValueError("Client ID and Tenant ID must be set in the .env file.")

app = PublicClientApplication(
    client_id,
    authority=f"https://login.microsoftonline.com/{tenant_id}"
)

# initialize result variable to hold the token response
result = None 

# 1. Revisar si hay cuentas en la cache
accounts = app.get_accounts()
if accounts:
    print("Cuenta encontrada en cache:")
    for a in accounts:
        print(f" - {a['username']}")
    chosen = accounts[0]
    result = app.acquire_token_silent(["User.Read"], account=chosen)

# 2. Si no hay token en cache, pedirlo de forma interactiva
if not result:
    print("No hay token en cache. Abriendo login interactivo...")
    result = app.acquire_token_interactive(scopes=["User.Read"])

# 3. Comprobar si se obtuvo el token e imprimirlo
if "access_token" in result:
    print("\n--- TOKEN OBTENIDO CON ÉXITO ---")
    print(result["access_token"])
else:
    print("\n--- ERROR AL OBTENER TOKEN ---")
    print("Error:", result.get("error"))
    print("Descripción:", result.get("error_description"))
    print("Correlation ID:", result.get("correlation_id"))