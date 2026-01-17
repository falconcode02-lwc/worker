GCP Service Account credentials (DEV)

This folder contains a placeholder `gcp-credentials.json` used for local development. Do NOT commit real service-account JSON files to source control. The `gcp-credentials.json` file is ignored via `.gitignore`.

Steps to use:

1) Replace the placeholder
   - Overwrite `gcp-credentials.json` with the service account JSON you downloaded from Google Cloud Console.

2) Set environment variables (PowerShell example):
   $env:GCP_CREDENTIALS_PATH = "${PWD}\src\main\resources\props-dev\vault\gcp-credentials.json"
   $env:GCP_PROJECT_ID = "your-gcp-project-id"

   Or set them in your OS environment or run configuration.

3) Ensure `src/main/resources/props-dev/vault/application-vault-gcp.properties` has:
   vault.gcp.credentials-path=${GCP_CREDENTIALS_PATH}
   vault.gcp.project-id=${GCP_PROJECT_ID}

4) Run the application (example using gradle):
   ./gradlew bootRun

5) To store a secret in GCP Secret Manager via the application's API:
   POST /api/secrets
   Content-Type: application/json

   {
     "name": "my-gcp-secret",
     "type": "TelegramMessagePlugin",
     "value": "{ \"secretName\": \"telegram001\", \"access_token\": \"abcd\", \"chat_id\": \"123\"}",
     "vaultType": "GCP"
   }

   Response: HTTP 201 with a SecretEntity referencing the GCP secret.

6) To verify the secret was stored in Google Secret Manager:
   - Use `gcloud`:
     gcloud secrets versions access latest --secret="my-gcp-secret" --project=${GCP_PROJECT_ID}

   - Or use Google Cloud Console -> Secret Manager UI.

Notes:
- The application saves a reference in the local DB (value like `gcp-sm:projects/.../secrets/.../versions/...`).
- The `GoogleSecretManagerWriter` reads credentials from the file at `vault.gcp.credentials-path` and creates the SecretManager client accordingly.

