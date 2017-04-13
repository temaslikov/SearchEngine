package ru.temaslikov.searchEngine;

/**
 * Created by Артём on 14.03.2017.
 */
public class TokenInfo {
    private Integer idToken;
    private Long Shift;

    public TokenInfo(Integer idToken, Long Shift) {
        this.idToken = idToken;
        this.Shift = Shift;
    }

    public Integer getIdToken() {
        return idToken;
    }

    public void setIdToken(Integer idToken) {
        this.idToken = idToken;
    }

    public Long getShift() {
        return Shift;
    }

    public void setShift(Long shift) {
        Shift = shift;
    }
}
