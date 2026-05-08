package sn.senannonces.annonceservice.model;


public enum AnnonceStatus {

    /** Newly created ad, awaiting moderation review. */
    EN_ATTENTE,

    /** Ad approved by a moderator and ready to be published. */
    APPROUVEE,

    /** Ad rejected by a moderator. Terminal state. */
    REJETEE,

    /** Ad published and visible to end users. Terminal state. */
    PUBLIEE
}
