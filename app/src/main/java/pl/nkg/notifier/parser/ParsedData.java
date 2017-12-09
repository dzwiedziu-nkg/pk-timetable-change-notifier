package pl.nkg.notifier.parser;

public class ParsedData {

    private ParsedEntity firstStage;
    private ParsedEntity secondStage;
    private ParsedEntity thirdStage;

    public ParsedEntity getFirstStage() {
        return firstStage;
    }

    public ParsedEntity getSecondStage() {
        return secondStage;
    }

    public ParsedEntity getThirdStage() {
        return thirdStage;
    }

    public void setFirstStage(ParsedEntity firstStage) {
        this.firstStage = firstStage;
    }

    public void setSecondStage(ParsedEntity secondStage) {
        this.secondStage = secondStage;
    }

    public void setSecondStage(ParsedEntity secondStage) {
        this.thirdStage = thirdStage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParsedData that = (ParsedData) o;

        if (this.checkStage(firstStage, that.firstStage))
            return false;
        if (this.checkStage(secondStage, that.secondStage))
            return false;
        return !this.checkStage(thirdStage, that.thirdStage);

    }

    @Override
    public int hashCode() {
        int result = firstStage != null ? firstStage.hashCode() : 0;
        result = 31 * result + (secondStage != null ? secondStage.hashCode() : 0);
        return result;
    }

    public boolean validate() {
        return (firstStage != null && secondStage != null && firstStage.validate() && secondStage.validate())
                || thirdStage != null && thirdStage.validate();
    }

    private boolean checkStage(ParsedEntity stage, ParsedEntity thatStage) {
        return stage!= null ? !stage.equals(thatStage) : thatStage != null;
    }
}

