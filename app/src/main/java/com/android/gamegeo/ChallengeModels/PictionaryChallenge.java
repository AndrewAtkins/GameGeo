package com.android.gamegeo.ChallengeModels;

public class PictionaryChallenge extends Challenge {
    private String image;
    private String secretWord;

    public PictionaryChallenge(String image, String secretWord, double latitude, double longitude, String id) {
        super(latitude, longitude, id);
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

    public static class Challenge {
        private double latitude;
        private double longitude;
        private String id;

        public Challenge(double latitude, double longitude, String id) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.id = id;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getId() {
            return id;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
