package modele;

import aes.Outils;

public record Config_AES(String motDePasse, String iv) {
    public byte[] getMotdepasse(){
        return Outils.normalizeChaine(motDePasse,16);
    }

    public byte[] getIV(){
        return Outils.normalizeChaine(iv,16);
    }
}
