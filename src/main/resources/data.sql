-- Insertar proyectos
INSERT INTO projects (title, description, link, github_repo, created_date, stack) VALUES 
('Portfolio Personal Profesional', 'Aplicación web completa de portafolio personal desarrollada con Angular 20 para el frontend y Spring Boot para el backend. Incluye gestión de proyectos, skills, experiencias y formulario de contacto con autenticación JWT para panel de administración.', 'https://portfolio-frontend-bernaruriza.vercel.app', 'https://github.com/BernardUriza/portfolio-frontend', '2024-12-01', 'Angular 20, Spring Boot, PostgreSQL, Tailwind CSS, JWT, Vercel'),

('VHouse - Sistema de Gestión Empresarial', 'Sistema integral de gestión para empresas que incluye manejo de inventarios, órdenes, clientes y reportes. Desarrollado con tecnologías modernas para optimizar procesos empresariales.', 'https://vhouse.example.com', 'https://github.com/BernardUriza/vhouse-system', '2024-08-15', 'React, Node.js, MongoDB, Express, Material-UI');

-- Insertar skills
INSERT INTO skills (name, description) VALUES 
('Angular', 'Framework de desarrollo frontend con TypeScript. Experiencia en Angular 15-20, manejo de componentes, servicios, routing, guards y testing con Jasmine/Karma.'),
('React', 'Biblioteca de JavaScript para construir interfaces de usuario. Experiencia con hooks, context, Redux, y ecosistema React completo.'),
('Spring Boot', 'Framework de Java para desarrollo de APIs REST. Experiencia con Spring Security, JPA/Hibernate, validaciones y arquitectura de microservicios.'),
('Node.js', 'Runtime de JavaScript para backend. Experiencia con Express.js, APIs RESTful, autenticación JWT y integración con bases de datos.'),
('PostgreSQL', 'Sistema de gestión de bases de datos relacional. Experiencia en diseño de esquemas, consultas complejas, optimización y procedimientos almacenados.'),
('MongoDB', 'Base de datos NoSQL orientada a documentos. Experiencia en modelado de datos, agregaciones, indexación e integración con aplicaciones Node.js.'),
('Git & GitHub', 'Control de versiones distribuido. Experiencia con flujos de trabajo Git, branching strategies, pull requests, GitHub Actions para CI/CD.'),
('Docker', 'Containerización de aplicaciones. Experiencia en creación de Dockerfiles, docker-compose, orquestación básica y despliegue de contenedores.');

-- Insertar experiencias
INSERT INTO experience (title, company, description) VALUES 
('Desarrollador Full Stack', 'Freelance / Proyectos Personales', 'Desarrollo de aplicaciones web completas utilizando Angular, React, Spring Boot y Node.js. Experiencia en diseño de APIs REST, manejo de bases de datos relacionales y NoSQL, implementación de autenticación y despliegue en la nube.'),
('Desarrollador Web Frontend', 'Proyectos Académicos y Personales', 'Creación de interfaces de usuario modernas y responsivas con Angular y React. Implementación de diseño UI/UX con Tailwind CSS y SCSS. Integración con APIs REST y manejo de estado de aplicaciones.');