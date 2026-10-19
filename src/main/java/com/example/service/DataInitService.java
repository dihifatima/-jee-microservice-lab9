package com.example.service;

import com.example.model.Auteur;
import com.example.model.Livre;

import javax.persistence.EntityManager;

/** Insère les données. La transaction est gérée par l'appelant (main). */
public class DataInitService {

    private final EntityManager em;

    public DataInitService(EntityManager em) {
        this.em = em;
    }

    public void initData() {
        for (int i = 1; i <= 10; i++) {
            Auteur auteur = new Auteur("Auteur " + i);
            for (int j = 1; j <= 5; j++) {
                auteur.ajouterLivre(new Livre("Livre " + j + " de l'auteur " + i, auteur));
            }
            em.persist(auteur);
        }
    }
}
