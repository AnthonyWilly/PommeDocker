# PommeDocker

Fork do projeto [Pomme](https://github.com/psoft-2025-2/projeto-psoft-pomme#) desenvolvido na disciplina de Projeto de Software (UFCG), 
utilizado como base para estudo e prática de containerização com Docker e Docker Compose.

## O que foi adicionado
- `Dockerfile` com multi-stage build para aplicação Spring Boot
- `docker-compose.yml` para orquestração do serviço

## Como rodar
```bash
docker compose up --build
```
A aplicação sobe na porta `http://localhost:8080`.
