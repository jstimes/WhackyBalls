package enterprises.stimes.whackyballs;

/**
 * Created by Jacob on 5/21/2016.
 *
 * Your standard vector class
 */
public class Vec3 {

    public float x, y, z;

    public Vec3(){
        x = y = z = 0.0f;
    }

    public Vec3(float c){
        x = y = z = c;
    }

    public Vec3(float a, float b, float c) {
        x = a;
        y = b;
        z = c;
    }

    public float dot(Vec3 r){
        return this.x * r.x + this.y * r.y + this.z * r.z;
    }

    public Vec3 cross(Vec3 r){
        Vec3 cross = new Vec3();
        cross.x = this.y * r.z - this.z * r.y;
        cross.y = this.z * r.x - this.x * r.z;
        cross.z = this.x * r.y - this.y * r.x;
        return cross;
    }

    public Vec3 add(Vec3 r){
        return new Vec3(this.x + r.x, this.y + r.y, this.z + r.z);
    }

    public Vec3 sub(Vec3 r){
        return new Vec3(this.x - r.x, this.y - r.y, this.z - r.z);
    }

    public Vec3 scalar(float scalar){
        return new Vec3(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public float magnitude(){
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public static float squaredDistance(Vec3 a, Vec3 b){
        Vec3 aSubB = a.sub(b);
        return aSubB.dot(aSubB);
    }
}
