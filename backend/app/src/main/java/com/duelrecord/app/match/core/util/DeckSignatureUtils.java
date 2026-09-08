package com.duelrecord.app.match.core.util;

import com.duelrecord.app.match.core.model.DeckCardRole;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DeckSignatureUtils {

    private DeckSignatureUtils() {
    }

    public record SignatureItem(UUID cardId, DeckCardRole role) {}

    public static String generateSignature(Collection<SignatureItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot generate signature for empty items");
        }

        String canonical = items.stream()
                .sorted((a, b) -> {
                    int roleCmp = a.role().name().compareTo(b.role().name());
                    if (roleCmp != 0) {
                        return roleCmp;
                    }
                    return a.cardId().compareTo(b.cardId());
                })
                .map(item -> item.role().name() + ":" + item.cardId().toString())
                .collect(Collectors.joining("|"));

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
