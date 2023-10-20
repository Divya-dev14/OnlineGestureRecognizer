
package recognizer;

import java.util.ArrayList;

public class GestureTemplate {

        //name of the gesture
	String name;
        //data points
	ArrayList<GesturePoint> points;


        //constructor
	public GestureTemplate(String ges_name, ArrayList<GesturePoint> ges_points) {
		this.name = ges_name;
		this.points = ges_points;

                //calling resampling method
		this.points = GestureComputing.resampling(this.points, CanvasBoard.N);
		double x = 0.0;
		double y = 0.0;

                // processing the points
		for(int i=0; i<points.size(); i++)
                {
			GesturePoint pt = points.get(i);
			x +=pt.x;
			y += pt.y;
		}

                //diving by data points size
		double qx = x / (double)points.size();
		double qy = y / (double)points.size();



		GesturePoint central_value = new GesturePoint(qx, qy);

		GesturePoint cur = points.get(0);

		double angleValue = Math.atan2(central_value.y - cur.y, central_value.x - cur.x);

                //calling rotation method
		this.points = GestureComputing.rotation(this.points, -1.0*angleValue);

                //calling scaling method
		this.points = GestureComputing.scaling(this.points, CanvasBoard.maxSize);

                //calling translation method
		this.points = GestureComputing.translation(this.points, new GesturePoint(0,0));

	}
}
