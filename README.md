# Collektar
<div align="center">
<img src="./frontend/public/collektar-logo.svg" alt="Collektar Logo" width="300" />
</div>

## Project Overview
Collektar is a modern and microservices-based platform for managing and organizing content across different media types.

## 📊 Code Quality

| Service | Status |
|---------|--------|
| **Authentication Service** | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=collektar_Authentication-Service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=collektar_Authentication-Service) |
| **Media Catalog Service** | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=collektar_Media-Catalog-Service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=collektar_Media-Catalog-Service) |
| **Collection Service** | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=collektar_Collection-Service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=collektar_Collection-Service) |
| **Frontend** | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=collektar_frontend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=collektar_frontend) |

---

## 📋 Table of Contents

- [Architecture](#️-architecture)
- [Repository Structure](#-repository-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Quick Start](#quick-start)
- [Frontend Development](#frontend--nextjs)

---

## 🏗️ Architecture

### Microservices Architecture
> [!NOTE]
> _Content coming soon_

### API Gateway
> [!NOTE]
> _Content coming soon_

### Tech Stack
- **Frontend**: Next.js, TypeScript, Tailwind CSS
- **Backend**: Kotlin Ktor
- **Database**: PostgreSQL
- **Infrastructure**: Docker, Docker Compose, Traefik

---

## 📁 Repository Structure
```
collektar/
├── backend/                      # backend microservices
│   ├── Authentication-Service/   
│   ├── Collection-Service/       
│   ├── Media-Catalog-Service/    
│   └── traefik/                  
├── frontend/                     # next.js frontend application
│   └── ...                      
├── traefik/                      # traefik reverse proxy config
│   └── dynamic/                              
├── docker-compose.yml            
├── Makefile                      # setup script
└── README.md                     # this file
```

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed on your system:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Node.js](https://nodejs.org/)
- [pnpm](https://pnpm.io/)
- [Make](https://www.gnu.org/software/make/) (usually pre-installed on Unix systems, consider using WSL on Windows)

### Quick Start

Follow these steps to get Collektar running locally:

#### 1. Clone the Repository
```bash
git clone git@github.com:ase-group-a/collektar.git
cd collektar
```

#### 2. Generate Required Keys and Certificates
```bash
make setup
```

This command generates necessary cryptographic keys required by the services.
(If you are curious, you can checkout `./config/` and `./secrets/`)

#### 3. Configure Environment Variables
```bash
cp .env.development .env
```

Edit the `.env` file and fill in the required values that are missing from the template:
```bash
# Example environment variables
EXTERNAL_API_KEY=super-secret-api-key
# ... add other required variables
```

#### 4. Start Backend Services
```bash
docker-compose up -d
```

> [!NOTE]
> Frontend service will be added to Docker Compose shortly. For now, run it separately (see [Frontend Development](#frontend--nextjs)).

#### 5. Verify Services

Check that all services are running:
```bash
docker-compose ps
```

You should see all services in a healthy state.

---

## Frontend / Next.js

If **pnpm** is not installed:
```bash
npm install -g pnpm
```

Select **pnpm** as package manager in IDE (optional), to prevent accidental npm commands:

<img src="/frontend/public/pnpm.png" alt="Select pnpm in IDE" width="500" />

Make sure you are in frontend folder:
```bash
cd frontend
```

Install dependencies:
```bash
pnpm install
```

Start the development server:
```bash
pnpm dev
```

Open [http://localhost:3000](http://localhost:3000)

Testing components with Jest and React testing library:
```bash
pnpm test
```

---

<div align="center">

**[⬆ back to top](#collektar)**

</div>
