package pl.nkg.notifier.parser;

public class ParsedData {

    private ParsedEntity firstStage;
    private ParsedEntity secondStage;

    public ParsedEntity getFirstStage() {
        return firstStage;
    }

    public ParsedEntity getSecondStage() {
        return secondStage;
    }

    public void setFirstStage(ParsedEntity firstStage) {
        this.firstStage = firstStage;
    }

    public void setSecondStage(ParsedEntity secondStage) {
        this.secondStage = secondStage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParsedData that = (ParsedData) o;

        if (firstStage != null ? !firstStage.equals(that.firstStage) : that.firstStage != null)
            return false;
        return !(secondStage != null ? !secondStage.equals(that.secondStage) : that.secondStage != null);

    }

    @Override
    public int hashCode() {
        int result = firstStage != null ? firstStage.hashCode() : 0;
        result = 31 * result + (secondStage != null ? secondStage.hashCode() : 0);
        return result;
    }
}
