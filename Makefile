.PHONY: setup clean help

CONFIG_DIR := config
SECRETS_DIR := secrets
PRIVATE_KEY := $(SECRETS_DIR)/jwt_private.pem
PUBLIC_KEY := $(CONFIG_DIR)/jwt_public.pem
TOKEN_HASHER_SECRET := $(SECRETS_DIR)/token_hasher_secret

all: setup

setup:
	@echo "Setting up Keys and Secrets"
	@mkdir -p $(CONFIG_DIR)
	@mkdir -p $(SECRETS_DIR)
	@if [ ! -f $(PRIVATE_KEY) ]; then \
		echo "Generating RSA private key..."; \
		openssl genrsa -out $(PRIVATE_KEY) 4096; \
		chmod 600 $(PRIVATE_KEY); \
		echo "Private key generated at $(PRIVATE_KEY)"; \
	else \
		echo "Private key already exists at $(PRIVATE_KEY)"; \
	fi
	@if [ ! -f $(PUBLIC_KEY) ]; then \
		echo "Extracting public key..."; \
		openssl rsa -in $(PRIVATE_KEY) -pubout -out $(PUBLIC_KEY); \
		chmod 644 $(PUBLIC_KEY); \
		echo "Public key generated at $(PUBLIC_KEY)"; \
	else \
		echo "Public key already exists at $(PUBLIC_KEY)"; \
	fi
	@if [ ! -f $(TOKEN_HASHER_SECRET) ]; then \
		echo "Generating TOKEN_HASHER_SECRET..."; \
		openssl rand -base64 32 | tr -d '\n' > $(TOKEN_HASHER_SECRET); \
		chmod 600 $(TOKEN_HASHER_SECRET); \
		echo "Token hasher secret generated at $(TOKEN_HASHER_SECRET)"; \
	else \
		echo "Token hasher secret already exists at $(TOKEN_HASHER_SECRET)"; \
	fi
	@echo "✓ Setup complete!"

clean:
	@echo "Warning: This will delete your JWT keys and secrets!"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		rm -f $(PRIVATE_KEY) $(PUBLIC_KEY) $(TOKEN_HASHER_SECRET); \
		echo "Keys and secrets deleted."; \
	else \
		echo "Cancelled."; \
	fi

help:
	@echo "Available targets:"
	@echo "  setup - Create required RSA key pair."
	@echo "  clean - Remove generated keys."
