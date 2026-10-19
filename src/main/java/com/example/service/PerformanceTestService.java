package com.example.service;

import com.example.model.Auteur;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class PerformanceTestService {

    private final EntityManager em;

    public PerformanceTestService(EntityManager em) {
        this.em = em;
    }

    /** N+1 : 1 requête pour les auteurs + 1 par auteur pour ses livres. */
    public void testPerformanceSansCache() {
        em.clear();
        long start = System.currentTimeMillis();

        List<Auteur> auteurs = em.createQuery("SELECT a FROM Auteur a", Auteur.class).getResultList();
        for (Auteur auteur : auteurs) {
            auteur.getLivres().size(); // provoque le chargement
        }

        System.out.println("Durée sans cache : " + (System.currentTimeMillis() - start) + " ms");
    }

    public void testPerformanceAvecJoinFetch() {
        em.clear();
        long start = System.currentTimeMillis();

        List<Auteur> auteurs = em.createQuery(
                "SELECT DISTINCT a FROM Auteur a JOIN FETCH a.livres", Auteur.class).getResultList();
        for (Auteur auteur : auteurs) {
            auteur.getLivres().size(); // aucune requête supplémentaire
        }

        System.out.println("Durée avec JOIN FETCH : " + (System.currentTimeMillis() - start) + " ms");
    }

    public void testPerformanceAvecEntityGraph() {
        em.clear();
        long start = System.currentTimeMillis();

        EntityGraph<?> graph = em.getEntityGraph("Auteur.livres");
        TypedQuery<Auteur> query = em.createQuery("SELECT DISTINCT a FROM Auteur a", Auteur.class);
        query.setHint("javax.persistence.fetchgraph", graph);
        List<Auteur> auteurs = query.getResultList();
        for (Auteur auteur : auteurs) {
            auteur.getLivres().size();
        }

        System.out.println("Durée avec EntityGraph : " + (System.currentTimeMillis() - start) + " ms");
    }

    public void testPerformanceAvecCache() {
        // 1re lecture : depuis la base, remplit le cache de 2e niveau
        testPerformanceSansCache();

        // 2e lecture : session vidée (1er niveau), donc lecture depuis le cache de 2e niveau
        em.clear();
        long start = System.currentTimeMillis();
        List<Auteur> auteurs = em.createQuery("SELECT a FROM Auteur a", Auteur.class)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
        for (Auteur auteur : auteurs) {
            auteur.getLivres().size();
        }
        System.out.println("Durée avec cache (lecture 2) : " + (System.currentTimeMillis() - start) + " ms");
    }
}
