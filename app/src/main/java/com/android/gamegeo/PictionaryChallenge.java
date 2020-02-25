package com.android.gamegeo;

public class PictionaryChallenge extends Challenge {
    private String image;
    private String secretWord;

    public PictionaryChallenge(String image, String secretWord, double latitude, double longitude) {
        super(latitude, longitude);
        this.image = image;
        this.secretWord = secretWord;
    }

    public String getImage() {
        return image;
    }

    public String getSecretWord() {
        return secretWord;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSecretWord(String secretWord) {
        this.secretWord = secretWord;
    }
}
