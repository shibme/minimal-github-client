package me.shib.lib.github.client.models;

import java.util.Date;
import java.util.List;

public final class Advisory {

    private Long databaseId;
    private String description;
    private String ghsaId;
    private String id;
    private String origin;
    private String permalink;
    private Date publishedAt;
    private Severity severity;
    private String summary;
    private Date updatedAt;
    private Date withdrawnAt;
    private List<Identifier> identifiers;

    public Long getDatabaseId() {
        return databaseId;
    }

    public String getDescription() {
        return description;
    }

    public String getGhsaId() {
        return ghsaId;
    }

    public String getId() {
        return id;
    }

    public String getOrigin() {
        return origin;
    }

    public String getPermalink() {
        return permalink;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getSummary() {
        return summary;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getWithdrawnAt() {
        return withdrawnAt;
    }

    public List<Identifier> getIdentifiers() {
        return identifiers;
    }

    public static class Identifier {
        private String type;
        private String value;

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }
}
