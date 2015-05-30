package pl.andrzejressel.wspolbiezne.processing;

import de.looksgood.ani.easing.Easing;

/**
 * Backup
 **/

public class FixedLinear extends Easing {

    public FixedLinear() {
        this.setMode(this.easingMode);
    }

    public float easeIn(float t, float b, float c, float d) {
        return this.easeNone(t, b, c, d);
    }

    public float easeOut(float t, float b, float c, float d) {
        return this.easeNone(t, b, c, d);
    }

    public float easeInOut(float t, float b, float c, float d) {
        return this.easeNone(t, b, c, d);
    }

    private float easeNone(float t, float b, float c, float d) {
        return t != d ? c * t / d + b : b + c;
    }

}
