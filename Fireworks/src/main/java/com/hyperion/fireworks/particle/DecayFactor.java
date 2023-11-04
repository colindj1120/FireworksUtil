package com.hyperion.fireworks.particle;

public enum DecayFactor {
    SUPER_SUPER_SLOW(0.001),
    SUPER_SLOW(0.005),
    SLOW(0.01),
    MEDIUM(0.03),
    FAST(0.05);

    private final double decayRate;

    DecayFactor(double decayRate) {
        this.decayRate = decayRate;
    }

    public double getDecayRate() {
        return decayRate;
    }
}
