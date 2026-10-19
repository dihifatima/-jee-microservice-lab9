package com.example;

import com.example.model.Auteur;
import com.example.model.Livre;
import com.example.service.DataInitService;
import com.example.service.PerformanceTestService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class App {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-cache-pu");
        EntityManager em = emf.createEntityManager();

        DataInitService dataInitService = new DataInitService(em);
        PerformanceTestService performanceTestService = new PerformanceTestService(em);

        // 1) Données de départ (10 auteurs x 5 livres)
        em.getTransaction().begin();
        dataInitService.initData();

        // 2) Données ajoutées / modifiées directement dans main
        Auteur hugo = new Auteur("Victor Hugo");
        hugo.ajouterLivre(new Livre("Les Misérables", hugo));
        hugo.ajouterLivre(new Livre("Notre-Dame de Paris", hugo));
        em.persist(hugo);

        Auteur camus = new Auteur("Albert Camus");
        camus.ajouterLivre(new Livre("L'Étranger", camus));
        camus.ajouterLivre(new Livre("La Peste", camus));
        em.persist(camus);

        Auteur premier = em.find(Auteur.class, 1L);
        premier.setNom("Naguib Mahfouz");
        premier.getLivres().get(0).setTitre("Impasse des deux palais");
        em.getTransaction().commit();

        System.out.println("--- Test sans cache ---");
        performanceTestService.testPerformanceSansCache();

        System.out.println("--- Test avec JOIN FETCH ---");
        performanceTestService.testPerformanceAvecJoinFetch();

        System.out.println("--- Test avec EntityGraph ---");
        performanceTestService.testPerformanceAvecEntityGraph();

        System.out.println("--- Test avec cache de second niveau ---");
        performanceTestService.testPerformanceAvecCache();

        em.close();
        emf.close();
    }
}
