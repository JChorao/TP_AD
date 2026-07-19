# Sistema de Aluguer de Veículos (Microservices) 🚗

Projeto desenvolvido no âmbito da Unidade Curricular de Aplicações Distribuídas / Arquitetura de Software. O projeto consiste numa plataforma completa para gestão de alugueres de veículos, assente numa arquitetura orientada a microsserviços.

> **Nota importante:** O foco principal do meu contributo neste projeto foi a **implementação de uma Arquitetura de Microsserviços**, garantindo a modularidade, escalabilidade e a comunicação eficiente entre os diferentes domínios do sistema.

## 🧠 Arquitetura e Serviços Implementados

### 1. API Gateway
Atua como o ponto de entrada único para o sistema, encaminhando os pedidos dos clientes para os respetivos microsserviços de forma transparente e segura.
*Ficheiros/Módulo:* `api-gateway/`.

### 2. Service Discovery (Eureka)
Implementado para o registo e descoberta dinâmica de todos os serviços do ecossistema em tempo de execução, permitindo que os serviços se comuniquem sem necessidade de conhecer os IPs exatos uns dos outros.
*Ficheiros/Módulo:* `eureka-server/`.

### 3. Frontend Web Service
Interface com o utilizador que consome as APIs dos serviços de backend. Gere a navegação e a apresentação de vistas para login, catálogo de veículos, gestão de alugueres, perfil e visualização de mapas.
*Ficheiros/Módulo:* `frontend-service/` (inclui templates como `cars.html`, `my-rentals.html`, `gps_map.html`, `login.html`).

### 4. Serviços de Domínio Core
Implementação isolada das regras de negócio de cada componente do sistema:
* **Gestão de Utilizadores:** Gere a autenticação, registo e perfis (`account-service/`).
* **Catálogo de Veículos:** Gere a informação, listagem e estado dos carros (`veiculos/`).
* **Gestão de Alugueres:** Controla as reservas e o ciclo de vida dos alugueres (`rental-service/`).

### 5. Serviços de Suporte
* **Serviço de Pagamentos:** Lida com a faturação e transações (`payment-service/`).
* **Serviço de Localização:** Faz o rastreamento das coordenadas físicas dos veículos (`gps-service/`).

## 🛠️ Tecnologias
* **Linguagem:** Java
* **Arquitetura & Frameworks:** Spring Boot, Spring Cloud (Netflix Eureka, API Gateway)
* **Interface:** Thymeleaf (HTML/CSS), Spring MVC
* **Build Tool:** Maven (`pom.xml`)
* **Orquestração e Deploy:** Docker & Docker Compose (`docker-compose.yml`, `dockerfile`)
