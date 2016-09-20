package enterprises.stimes.whackyballs;

/**
 * Created by Jacob on 5/24/2016.
 *
 * Eventually will contain all the logic for AI ball's planning their own motion
 */
public class AIBall extends Ball {

    //Number of times the ball has updated its velocity in the same general direction as its previous direction
    private int accelerations = 0;

    public AIBall(float radius){
        super(radius);
    }
}
