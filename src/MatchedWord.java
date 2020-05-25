public class MatchedWord {
    private int startIndex;
    private int endIndex;
    private String word;

    public MatchedWord() {
    }

    public MatchedWord(int startIndex, int endIndex, String word) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.word = word;
    }

    public int start() {
        return startIndex;
    }

    public int end() {
        return endIndex;
    }

    @Override
    public String toString() {
        return word;
    }
}
