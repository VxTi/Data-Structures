package project.data;

import static java.lang.Math.*;

// Class for fast floating point arithmetic
public class FMath {

    private static final int resolution = 0x8000;
    private static final float[] sine_map = new float[resolution];

    public static final float TWO_PI        = 6.28318531f;
    public static final float ONE_OVER_2PI  = 0.159154943f;
    public static final float F_PI          = 3.141592653f;

    static {
        for (int i = 0; i < resolution; i++) {
            sine_map[i] = (float) Math.sin(TWO_PI / resolution * i);
        }
    }

    public static float fsin(float rads) {
        return sine_map[(int) ((float) resolution * ONE_OVER_2PI * rads) & (resolution - 1)];
    }

    public static float fcos(float rads) {
        return sine_map[(int) ((float) resolution * ONE_OVER_2PI * (rads +  1.57079633)) & (resolution - 1)];
    }

    public static double clamp(double x, double min, double max) {
        return Math.max(Math.min(x, max), min);
    }

    public static int round(double x) {
        return (int) (x + 0.5);
    }

    /**
     * Since floating point comparisons are so incredibly annoying,
     * we have to try and find a way around it. This is one way. <br>
     * Check whether |Δx| <= n with n -> 0
     */
    public static boolean double_equals(double x, double y, double delta) {
        return Math.abs(x - y) <= delta;
    }
    
    
    
    public static class Complex {

        public double real, complex;

        public Complex(double real, double complex) {
            this.real = real;
            this.complex = complex;
        }

        public Complex(double angle) {
            this.real = cos(angle);
            this.complex = sin(angle);
        }

        public FMath.Complex divide(FMath.Complex other) {
            if (this.complex == 0 || other.complex == 0)
                return new FMath.Complex(this.real / other.real, 0);

            if (this.real == 0 || other.real == 0)
                return new FMath.Complex(0, this.complex / other.complex);

            FMath.Complex ret = new FMath.Complex(this.real * other.real - this.complex * other.complex,
                    -this.real * other.complex - other.real * this.complex);
            double divisor = other.real * other.real - other.complex * other.complex;

            ret.real    /= divisor;
            ret.complex /= divisor;

            return ret;
        }

        public FMath.Complex subtract(FMath.Complex other) {
            return new FMath.Complex(this.real - other.real, this.complex - other.complex);
        }

        public FMath.Complex add(FMath.Complex other) {
            this.real += other.real;
            this.complex += other.complex;
            return this;
        }
        public FMath.Complex add_c(FMath.Complex other) {
            return new FMath.Complex(this.real + other.real, this.complex + other.complex);
        }

        public FMath.Complex multiply(FMath.Complex other) {
            double n_r = real * other.real - this.complex * other.complex;
            double n_i = this.real * other.complex + this.complex * other.real;
            this.real = n_r;
            this.complex = n_i;
            return this;
        }

        public FMath.Complex scale(double x) {
            return new FMath.Complex(this.real * x, this.complex * x);
        }

        public FMath.Complex multiply_c(FMath.Complex other) {
            return new FMath.Complex(real * other.real - this.complex * other.complex, // (a + bi)(c + di) = ac + adi + bci + (-bd)
                    this.real * other.complex + this.complex * other.real);
        }

        public FMath.Complex multiplyReal(double x) {
            return new FMath.Complex(this.real * x, this.complex * x);
        }

        public FMath.Complex multiplyComplex(double x) { // (a + bi) * (ci) = aci - bc
            return new FMath.Complex(-this.complex * x, this.real * x);
        }

        public double Re() { return this.real; }

        public double Im() { return this.complex; }

        @Override
        public String toString() {
            return "[" + real + (signum(complex) > 0 ? " + " : "") + complex + "i]";
        }
    }
}
