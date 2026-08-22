# Nginx Deploy React + Spring Boot + Mysql

This project demonstrates how to deploy a React frontend and Spring Boot backend using **Ubuntu/WSL, Nginx, HTTPS, and Let's Encrypt**.

The project can be used for local development with WSL and can also be deployed to a production Ubuntu server.



# Architecture

The final production architecture is:

```text
                         Internet
                            │
                            ▼
                     orbit.webs.vc
                            │
                          HTTPS
                            │
                            ▼
                         Nginx
                            │
                ┌───────────┴───────────┐
                │                       │
                ▼                       ▼
          React Frontend             /api/*
                │                       │
                │                       ▼
                │                Spring Boot
                │                  :8089
                │                       │
                │                       ▼
                │                    MySQL
                │
                ▼
             Browser
```

The important idea is:

```text
https://orbit.webs.vc
        │
        ├── /              → React
        │
        └── /api/*         → Spring Boot
```

Nginx acts as the reverse proxy between the public website and the backend.



# 1. Setting Up WSL and Ubuntu

This project uses **WSL (Windows Subsystem for Linux)** to run an Ubuntu environment directly on Windows.

## Install WSL

Open **PowerShell as Administrator**:

```powershell
wsl --install
```

Restart Windows if required.

## Check WSL

```powershell
wsl
```

You should see something similar to:

```text
username@COMPUTER:/mnt/c/Users/Username$
```



# 2. Install Nginx

Inside Ubuntu:

```bash
sudo apt update
sudo apt install nginx -y
```

Check Nginx:

```bash
nginx -v
```

Check its status:

```bash
sudo systemctl status nginx
```



# 3. Install Node.js Inside Ubuntu

To make sure Node.js runs inside Ubuntu/WSL rather than using the Windows Node.js installation, use **NVM (Node Version Manager)**.

## Install Required Packages

```bash
sudo apt update
sudo apt install curl -y
```

## Install NVM

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
```

## Reload Your Shell

```bash
source ~/.bashrc
```

## Check NVM

```bash
nvm --version
```

Example:

```text
0.40.3
```

## Install Node.js LTS

```bash
nvm install --lts
```

## Activate Node.js LTS

```bash
nvm use --lts
```



# 4. Verify Node.js Is Running Inside Ubuntu

Check the location of Node.js and npm:

```bash
which node
which npm
```

You should see something similar to:

```text
/home/ubuntu/.nvm/versions/node/v22.x.x/bin/node
/home/ubuntu/.nvm/versions/node/v22.x.x/bin/npm
```

You should **NOT** see:

```text
/mnt/c/Program Files/nodejs/
```

If you see `/mnt/c/Program Files/nodejs/`, Ubuntu is using the Windows Node.js installation instead of the Linux installation.



# 5. Install React Dependencies

Go to the project:

```bash
cd ~/nginx-deploy-react-starter
```

Remove dependencies that may have been installed using Windows Node.js:

```bash
rm -rf node_modules
```

Install them again using the Linux Node.js installation:

```bash
npm install
```



# 6. Run the React Development Server

Start Vite and make it listen on all interfaces:

```bash
npm run dev -- --host 0.0.0.0
```

Vite will normally start on:

```text
http://localhost:5173
```



# 7. React Environment Variables

For Vite, environment variables must start with:

```text
VITE_
```

## Development

Create:

```text
.env
```

Example:

```env
VITE_BACKEND_URL=http://localhost:8089
```

Use it in React:

```javascript
const VITE_BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
```

Example API request:

```javascript
const response = await fetch(`${VITE_BACKEND_URL}/products`);
const data = await response.json();
```



# 8. Build the React Application

Install dependencies first:

```bash
npm install
```

Then build:

```bash
npm run build
```

Vite generates:

```text
dist/
```

The `dist` directory contains the production frontend.



# 9. Nginx Configuration

Nginx configuration files are located at:

```bash
cd /etc/nginx/sites-available/
```

Create a configuration:

```bash
sudo nano /etc/nginx/sites-available/react-demo
```

Example configuration:

```nginx
server {
    listen 80;
    server_name orbit.webs.vc;

    root /var/www/react-demo;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8089/;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```



# 10. Enable the Nginx Site

Create the symbolic link:

```bash
sudo ln -s /etc/nginx/sites-available/react-demo /etc/nginx/sites-enabled/react-demo
```

Check the configuration:

```bash
sudo nginx -t
```

If successful:

```text
syntax is ok
test is successful
```

Reload Nginx:

```bash
sudo systemctl reload nginx
```



# 11. Deploy the React Build

Create the web directory:

```bash
sudo mkdir -p /var/www/react-demo
```

Remove the previous build:

```bash
sudo rm -rf /var/www/react-demo/*
```

Copy the new React build:

```bash
sudo cp -r dist/* /var/www/react-demo/
```

Reload Nginx:

```bash
sudo systemctl reload nginx
```



# 12. Spring Boot Backend

The Spring Boot backend runs locally on port `8089`.

Example:

```yaml
server:
  port: 8089
```

Test the backend directly:

```bash
curl http://localhost:8089/products
```

Example response:

```json
[]
```

The backend does not need to be publicly exposed on port `8089`.

Nginx handles public API requests.



# 13. API Reverse Proxy

The React application calls:

```text
https://orbit.webs.vc/api/products
```

Nginx receives the request:

```text
/api/products
```

and forwards it to:

```text
http://localhost:8089/products
```

The flow is:

```text
Browser
   │
   ▼
https://orbit.webs.vc/api/products
   │
   ▼
Nginx
   │
   ▼
http://localhost:8089/products
   │
   ▼
Spring Boot
```



# 14. Production React Environment

For production, use:

```env
VITE_BACKEND_URL=https://orbit.webs.vc/api
```

Then:

```javascript
const response = await fetch(`${VITE_BACKEND_URL}/products`);
```

will call:

```text
https://orbit.webs.vc/api/products
```

After changing `.env`, rebuild the application:

```bash
npm run build
```

Then redeploy `dist/`.



# 15. HTTPS with Let's Encrypt

Install Certbot:

```bash
sudo apt update
sudo apt install certbot python3-certbot-nginx -y
```

Generate the certificate:

```bash
sudo certbot --nginx -d orbit.webs.vc
```

Certbot configures HTTPS in Nginx automatically.

Certificate files are stored under:

```text
/etc/letsencrypt/live/orbit.webs.vc/
```

The important files are:

```text
fullchain.pem
privkey.pem
```



# 16. HTTP → HTTPS

After configuring Certbot, HTTP requests are redirected to HTTPS:

```text
http://orbit.webs.vc
        │
        ▼
   301 Redirect
        │
        ▼
https://orbit.webs.vc
```

Test:

```bash
curl -I http://orbit.webs.vc
```

You should see:

```text
HTTP/1.1 301 Moved Permanently
Location: https://orbit.webs.vc/
```

Test HTTPS:

```bash
curl -I https://orbit.webs.vc
```



# 17. Test the API

Test Spring Boot directly:

```bash
curl http://localhost:8089/products
```

Test through Nginx:

```bash
curl https://orbit.webs.vc/api/products
```

Expected:

```json
[]
```

This confirms:

```text
Internet
   ↓
HTTPS
   ↓
Nginx
   ↓
Spring Boot
   ↓
MySQL
```



# 18. Test Nginx

Every time the Nginx configuration is changed:

```bash
sudo nginx -t
```

If successful:

```text
nginx: configuration file /etc/nginx/nginx.conf
syntax is ok
nginx: configuration file /etc/nginx/nginx.conf
test is successful
```

Then reload:

```bash
sudo systemctl reload nginx
```



# 19. Useful Nginx Commands

Go to the available sites:

```bash
cd /etc/nginx/sites-available/
```

List configurations:

```bash
ls
```

View the configuration:

```bash
sudo cat /etc/nginx/sites-available/react-demo
```

Edit the configuration:

```bash
sudo nano /etc/nginx/sites-available/react-demo
```

Check enabled sites:

```bash
ls -l /etc/nginx/sites-enabled/
```

Check listening ports:

```bash
sudo ss -ltnp
```

Check port 80:

```bash
sudo ss -ltnp | grep ':80 '
```

Check port 443:

```bash
sudo ss -ltnp | grep ':443 '
```



# 20. Useful Deployment Commands

### Build React

```bash
npm install
npm run build
```

### Deploy React

```bash
sudo rm -rf /var/www/react-demo/*
sudo cp -r dist/* /var/www/react-demo/
```

### Test Nginx

```bash
sudo nginx -t
```

### Reload Nginx

```bash
sudo systemctl reload nginx
```

### Test backend

```bash
curl http://localhost:8089/products
```

### Test production API

```bash
curl https://orbit.webs.vc/api/products
```



# 21. Production Architecture

The final deployment is:

```text
                         Internet
                            │
                            ▼
                     orbit.webs.vc
                            │
                          HTTPS
                            │
                            ▼
                         Nginx
                            │
                ┌───────────┴───────────┐
                │                       │
                ▼                       ▼
          React Frontend             /api/*
                │                       │
                │                       ▼
                │                Spring Boot
                │                  :8089
                │                       │
                │                       ▼
                │                    MySQL
                │
                ▼
             Browser
```

The backend remains on:

```text
localhost:8089
```

and is accessed publicly through:

```text
https://orbit.webs.vc/api/*
```



# 22. Future Improvements

The current setup is a good foundation for a production deployment.

Possible improvements include:

- CI/CD with GitHub Actions
- Docker
- Docker Compose
- systemd services
- Automated deployments
- Database backups
- Monitoring
- Centralized logging
- Health checks
- Secrets management
- Firewall configuration
- Load balancing
- Multiple backend instances
- Cloudflare
- Kubernetes

A future CI/CD workflow could be:

```text
Developer
    │
    ▼
Git Push
    │
    ▼
GitHub
    │
    ▼
GitHub Actions
    │
    ├── Build React
    ├── Build Spring Boot
    ├── Run tests
    └── Deploy
          │
          ▼
       Ubuntu
          │
          ▼
        Nginx
```



# 23. Final Deployment Flow

The complete deployment follows:

```text
DNS
 ↓
HTTPS
 ↓
Nginx
 ↓
React
 ↓
/api
 ↓
Spring Boot
 ↓
MySQL
```

This setup demonstrates a common approach to hosting a full-stack application on an Ubuntu server, with Nginx acting as the public web server and reverse proxy.
