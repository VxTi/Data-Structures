package project.data;

public class Vector3d {
    public double x, y, z;

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3d() {
        this.x = this.y = this.z = 0;
    }

    public Vector3d translate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3d add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3d mult(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vector3d scale(double n) {
        this.x *= n;
        this.y *= n;
        this.z *= n;
        return this;
    }

    public Vector3d normalize() {
        double dst = Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
        this.x /= dst;
        this.y /= dst;
        this.z /= dst;
        return this;
    }

    public Vector3d copy() {
        return new Vector3d(this.x, this.y, this.z);
    }
}
