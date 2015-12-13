package pl.nkg.notifier.parser;

import java.net.URL;
import java.util.Date;

public class ParsedEntity {
    private URL url;
    private Date date;

    public ParsedEntity() {
    }

    public ParsedEntity(URL url, Date date) {
        this.url = url;
        this.date = date;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParsedEntity that = (ParsedEntity) o;

        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return !(date != null ? !date.equals(that.date) : that.date != null);

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
