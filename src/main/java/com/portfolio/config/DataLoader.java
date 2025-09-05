package com.portfolio.config;

import com.portfolio.model.Project;
import com.portfolio.model.Skill;
import com.portfolio.model.Experience;
import com.portfolio.repository.ProjectRepository;
import com.portfolio.repository.SkillRepository;
import com.portfolio.repository.ExperienceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo cargar datos si las tablas están vacías
        if (projectRepository.count() == 0) {
            loadProjects();
        }
        
        if (skillRepository.count() == 0) {
            loadSkills();
        }
        
        if (experienceRepository.count() == 0) {
            loadExperiences();
        }
    }

    private void loadProjects() {
        // Proyecto 1: Este mismo portafolio
        Project portfolioProject = Project.builder()
                .title("Portfolio Personal Profesional")
                .description("Aplicación web completa de portafolio personal desarrollada con Angular 20 para el frontend y Spring Boot para el backend. Incluye gestión de proyectos, skills, experiencias y formulario de contacto con autenticación JWT para panel de administración.")
                .link("https://portfolio-frontend-bernaruriza.vercel.app")
                .githubRepo("https://github.com/BernardUriza/portfolio-frontend")
                .createdDate(LocalDate.of(2024, 12, 1))
                .stack("Angular 20, Spring Boot, PostgreSQL, Tailwind CSS, JWT, Vercel")
                .build();

        // Proyecto 2: Sistema de gestión empresarial (ejemplo)
        Project vhouseProject = Project.builder()
                .title("VHouse - Sistema de Gestión Empresarial")
                .description("Sistema integral de gestión para empresas que incluye manejo de inventarios, órdenes, clientes y reportes. Desarrollado con tecnologías modernas para optimizar procesos empresariales.")
                .link("https://vhouse.example.com")
                .githubRepo("https://github.com/BernardUriza/vhouse-system")
                .createdDate(LocalDate.of(2024, 8, 15))
                .stack("React, Node.js, MongoDB, Express, Material-UI")
                .build();

        projectRepository.save(portfolioProject);
        projectRepository.save(vhouseProject);
        
        System.out.println("✅ Proyectos cargados exitosamente");
    }

    private void loadSkills() {
        // Skills Frontend
        Skill angular = Skill.builder()
                .name("Angular")
                .description("Framework de desarrollo frontend con TypeScript. Experiencia en Angular 15-20, manejo de componentes, servicios, routing, guards y testing con Jasmine/Karma.")
                .build();

        Skill react = Skill.builder()
                .name("React")
                .description("Biblioteca de JavaScript para construir interfaces de usuario. Experiencia con hooks, context, Redux, y ecosistema React completo.")
                .build();

        // Skills Backend
        Skill springBoot = Skill.builder()
                .name("Spring Boot")
                .description("Framework de Java para desarrollo de APIs REST. Experiencia con Spring Security, JPA/Hibernate, validaciones y arquitectura de microservicios.")
                .build();

        Skill nodejs = Skill.builder()
                .name("Node.js")
                .description("Runtime de JavaScript para backend. Experiencia con Express.js, APIs RESTful, autenticación JWT y integración con bases de datos.")
                .build();

        // Skills Base de Datos
        Skill postgresql = Skill.builder()
                .name("PostgreSQL")
                .description("Sistema de gestión de bases de datos relacional. Experiencia en diseño de esquemas, consultas complejas, optimización y procedimientos almacenados.")
                .build();

        Skill mongodb = Skill.builder()
                .name("MongoDB")
                .description("Base de datos NoSQL orientada a documentos. Experiencia en modelado de datos, agregaciones, indexación y integración con aplicaciones Node.js.")
                .build();

        // Skills DevOps/Herramientas
        Skill git = Skill.builder()
                .name("Git & GitHub")
                .description("Control de versiones distribuido. Experiencia con flujos de trabajo Git, branching strategies, pull requests, GitHub Actions para CI/CD.")
                .build();

        Skill docker = Skill.builder()
                .name("Docker")
                .description("Containerización de aplicaciones. Experiencia en creación de Dockerfiles, docker-compose, orquestación básica y despliegue de contenedores.")
                .build();

        skillRepository.save(angular);
        skillRepository.save(react);
        skillRepository.save(springBoot);
        skillRepository.save(nodejs);
        skillRepository.save(postgresql);
        skillRepository.save(mongodb);
        skillRepository.save(git);
        skillRepository.save(docker);

        System.out.println("✅ Skills cargadas exitosamente");
    }

    private void loadExperiences() {
        Experience fullStackDev = new Experience(
                null,
                "Desarrollador Full Stack",
                "Freelance / Proyectos Personales",
                "Desarrollo de aplicaciones web completas utilizando Angular, React, Spring Boot y Node.js. Experiencia en diseño de APIs REST, manejo de bases de datos relacionales y NoSQL, implementación de autenticación y despliegue en la nube. Enfoque en mejores prácticas de desarrollo, testing y arquitectura de software."
        );

        Experience webDeveloper = new Experience(
                null,
                "Desarrollador Web Frontend",
                "Proyectos Académicos y Personales",
                "Creación de interfaces de usuario modernas y responsivas con Angular y React. Implementación de diseño UI/UX con Tailwind CSS y SCSS. Integración con APIs REST, manejo de estado de aplicaciones y optimización de rendimiento web."
        );

        experienceRepository.save(fullStackDev);
        experienceRepository.save(webDeveloper);

        System.out.println("✅ Experiencias cargadas exitosamente");
    }
}