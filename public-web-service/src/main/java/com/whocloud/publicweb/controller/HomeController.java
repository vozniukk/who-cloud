package com.whocloud.publicweb.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
    
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String home() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>WHO Cloud Platform</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: #333;
                        line-height: 1.6;
                    }
                    
                    .header {
                        background: rgba(255, 255, 255, 0.98);
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        padding: 1rem 0;
                        position: sticky;
                        top: 0;
                        z-index: 100;
                    }
                    
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                        padding: 0 2rem;
                    }
                    
                    .nav {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                    
                    .logo {
                        font-size: 1.5rem;
                        font-weight: 700;
                        color: #667eea;
                        display: flex;
                        align-items: center;
                        gap: 0.5rem;
                    }
                    
                    .nav-links {
                        display: flex;
                        gap: 2rem;
                        list-style: none;
                    }
                    
                    .nav-links a {
                        color: #555;
                        text-decoration: none;
                        font-weight: 500;
                        transition: color 0.3s;
                    }
                    
                    .nav-links a:hover {
                        color: #667eea;
                    }
                    
                    .btn-login {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 0.6rem 1.5rem;
                        border-radius: 8px;
                        text-decoration: none;
                        font-weight: 600;
                        transition: transform 0.3s, box-shadow 0.3s;
                        display: inline-block;
                    }
                    
                    .btn-login:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
                    }
                    
                    .hero {
                        text-align: center;
                        padding: 6rem 2rem;
                        color: white;
                    }
                    
                    .hero h1 {
                        font-size: 3.5rem;
                        margin-bottom: 1.5rem;
                        font-weight: 700;
                        text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
                    }
                    
                    .hero p {
                        font-size: 1.3rem;
                        margin-bottom: 2.5rem;
                        opacity: 0.95;
                    }
                    
                    .features {
                        background: white;
                        padding: 5rem 2rem;
                    }
                    
                    .features-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                        gap: 2rem;
                        margin-top: 3rem;
                    }
                    
                    .feature-card {
                        background: #f8f9fa;
                        padding: 2rem;
                        border-radius: 12px;
                        text-align: center;
                        transition: transform 0.3s, box-shadow 0.3s;
                    }
                    
                    .feature-card:hover {
                        transform: translateY(-5px);
                        box-shadow: 0 10px 30px rgba(0,0,0,0.1);
                    }
                    
                    .feature-icon {
                        font-size: 3rem;
                        margin-bottom: 1rem;
                    }
                    
                    .feature-card h3 {
                        color: #667eea;
                        margin-bottom: 1rem;
                        font-size: 1.5rem;
                    }
                    
                    .feature-card p {
                        color: #666;
                        line-height: 1.8;
                    }
                    
                    .section-title {
                        text-align: center;
                        font-size: 2.5rem;
                        color: #333;
                        margin-bottom: 1rem;
                    }
                    
                    .section-subtitle {
                        text-align: center;
                        color: #666;
                        font-size: 1.1rem;
                    }
                    
                    .footer {
                        background: #2d3436;
                        color: white;
                        text-align: center;
                        padding: 2rem;
                    }
                    
                    @media (max-width: 768px) {
                        .hero h1 {
                            font-size: 2rem;
                        }
                        
                        .nav-links {
                            display: none;
                        }
                    }
                </style>
            </head>
            <body>
                <header class="header">
                    <div class="container">
                        <nav class="nav">
                            <div class="logo">
                                🏥 WHO Cloud
                            </div>
                            <ul class="nav-links">
                                <li><a href="/">Home</a></li>
                                <li><a href="/api/public/welcome">API</a></li>
                                <li><a href="/actuator/health">Status</a></li>
                            </ul>
                            <a href="/login/oauth2/authorization/google" class="btn-login">Sign In</a>
                        </nav>
                    </div>
                </header>
                
                <section class="hero">
                    <div class="container">
                        <h1>🔐 WHO Cloud Platform</h1>
                        <p>Enterprise-Grade Microservices Architecture with OAuth 2.0 Authentication</p>
                        <a href="/login/oauth2/authorization/google" class="btn-login" style="font-size: 1.1rem; padding: 0.8rem 2rem;">
                            Get Started →
                        </a>
                    </div>
                </section>
                
                <section class="features">
                    <div class="container">
                        <h2 class="section-title">Platform Features</h2>
                        <p class="section-subtitle">Built with modern cloud-native technologies</p>
                        
                        <div class="features-grid">
                            <div class="feature-card">
                                <div class="feature-icon">🔒</div>
                                <h3>Secure Authentication</h3>
                                <p>OAuth 2.0 integration with Google. JWT-based token authentication with role-based access control (RBAC).</p>
                            </div>
                            
                            <div class="feature-card">
                                <div class="feature-icon">⚡</div>
                                <h3>High Performance</h3>
                                <p>Nginx reverse proxy with rate limiting. Optimized microservices architecture for scalability.</p>
                            </div>
                            
                            <div class="feature-card">
                                <div class="feature-icon">🏗️</div>
                                <h3>Microservices</h3>
                                <p>12 specialized services: API Gateway, Auth Service, Business Services, Information Services.</p>
                            </div>
                            
                            <div class="feature-card">
                                <div class="feature-icon">🛡️</div>
                                <h3>Enterprise Security</h3>
                                <p>Security headers, CORS configuration, SSL/TLS support, and DDoS protection.</p>
                            </div>
                            
                            <div class="feature-card">
                                <div class="feature-icon">📊</div>
                                <h3>Monitoring</h3>
                                <p>Health checks, metrics, and logging. Real-time service status monitoring.</p>
                            </div>
                            
                            <div class="feature-card">
                                <div class="feature-icon">🚀</div>
                                <h3>Cloud Ready</h3>
                                <p>Docker containerization, PostgreSQL database, and production-ready deployment.</p>
                            </div>
                        </div>
                    </div>
                </section>
                
                <footer class="footer">
                    <div class="container">
                        <p>&copy; 2026 WHO Cloud Platform. All rights reserved.</p>
                        <p style="margin-top: 0.5rem; opacity: 0.8;">Powered by Spring Boot, Spring Cloud Gateway & OAuth 2.0</p>
                    </div>
                </footer>
            </body>
            </html>
        """;
    }
}
