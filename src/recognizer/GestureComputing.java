
package recognizer;

import java.util.ArrayList;

public class GestureComputing {

            // recognizing method
	public static GestureScore recognizing(ArrayList<GesturePoint> points) {

		double best = Double.MAX_VALUE;
		int templateIndex = Integer.MIN_VALUE;
		int i=0;
		int n = CanvasBoard.templates.size();
                //processing the points
		while(i < n)
                {
			double angleDistance = 0.0;
			angleDistance = helper(points, -1.0*CanvasBoard.theta, CanvasBoard.theta, CanvasBoard.dtheta, CanvasBoard.templates.get(i));
			if(angleDistance < best) {
				best = angleDistance;
				templateIndex = i;
			}
			i++;
		}

                //Not recognizable condition
		if(templateIndex == Integer.MIN_VALUE)
                {
			return new GestureScore("Not recognizable", 0.0);
		}

                //computing score
		double score = (1.0 - best) / CanvasBoard.half;
		return new GestureScore(CanvasBoard.templates.get(templateIndex).name, score);

	}

        // resampling method
	public static ArrayList<GesturePoint> resampling(ArrayList<GesturePoint> points, int max){

		double dist = 0.0;

		for(int i=1; i<points.size(); i++)
                {
			GesturePoint a = points.get(i-1);
			GesturePoint b = points.get(i);
			double dx = b.x - a.x;
			double dy = b.y - a.y;
			dist += Math.sqrt(dx*dx + dy*dy);
		}

		double top = dist;
		double index = top / (double)(max-1);
		double best = 0.0;

		ArrayList<GesturePoint> points2 = new ArrayList<>();
		GesturePoint pt = points.get(0);
		points2.add(pt);
		int i=1;
                //resampling
		while(i < points.size())
                {
			GesturePoint a = points.get(i-1);
			GesturePoint b = points.get(i);
			double dx = b.x - a.x;
			double dy = b.y - a.y;
			double d =  Math.sqrt(dx*dx + dy*dy);

                        //processing the points
			if((best + d) >= index)
                        {
				double x = 0.0;
				double y = 0.0;
				x = (double)a.x + ((index-best)/d) * (b.x - a.x);
				y = (double)a.y + ((index-best)/d) * (b.y - a.y);
				GesturePoint q = new GesturePoint(x, y);
				points2.add(q);
				points.add(i, q);
				best = 0.0;
			}
			else
                        {
				best += d;
			}
			i++;
		}

		if(points2.size() == max-1)
                {
			points2.add(points.get(points.size()-1));

		}
		return points2;
	}

	 // rotation method
	public static ArrayList<GesturePoint> rotation(ArrayList<GesturePoint> points, double dx)
        {
		double x = 0.0;
		double y = 0.0;
		for(int i=0; i<points.size(); i++)
                {
			GesturePoint pt = points.get(i);
			x +=pt.x;
			y += pt.y;
		}
		double qx = x / (double)points.size();
		double qy = y / (double)points.size();

                //computing central point
		GesturePoint central = new GesturePoint(qx, qy);
		double cosValue = Math.cos(dx);
		double sinValue = Math.sin(dx);
		ArrayList<GesturePoint> points2 = new ArrayList<>();

		int i=0;
                //processing the points
		while( i < points.size())
                {
			double x2 = 0.0;
			double y2 = 0.0;
			GesturePoint p = points.get(i);
			x2 = (p.x - central.x) * cosValue - (p.y - central.y) * sinValue + central.x;
			y2 = (p.x - central.x) * sinValue + (p.y - central.y) * cosValue + central.y;
			GesturePoint p2 = new GesturePoint(x2,y2);
			points2.add(p2);
			i++;
		}
		return points2;
	}

	 // scaling method
	public static ArrayList<GesturePoint> scaling(ArrayList<GesturePoint> points, double max) {
		double x1 = Double.MAX_VALUE, x2 = Double.MIN_VALUE, y1 = Double.MAX_VALUE, y2 = Double.MIN_VALUE;
		for (int i = 0; i < points.size(); i++)
                {
			x1 = Math.min(x1, points.get(i).x);
			y1 = Math.min(y1, points.get(i).y);
			x2 = Math.max(x2, points.get(i).x);
			y2 = Math.max(y2, points.get(i).y);
		}
		double w = 0.0;
		double h = 0.0;
		w= x2 - x1;
		h = y2 - y1;
		ArrayList<GesturePoint> points2 = new ArrayList<>();
		int i=0;
                //processing the points
		while(i < points.size())
                {
			GesturePoint p = points.get(i);
			double x = 0.0;
			double y = 0.0;
			x = (double)p.x * (max / w); //scaling
			y = (double)p.y * (max / h);
			GesturePoint pt = new GesturePoint(x,y);
			points2.add(pt);
			i++;
		}
		return points2;
	}

        //translation method
	public static ArrayList<GesturePoint> translation(ArrayList<GesturePoint> points, GesturePoint pt){
		double x = 0.0;
		double y = 0.0;

		for(int i=0; i<points.size(); i++)
                {
			GesturePoint p = points.get(i);
			x +=p.x;
			y += p.y;
		}
		double qx = x / (double)points.size();
		double qy = y / (double)points.size();

		GesturePoint central = new GesturePoint(qx, qy);
		ArrayList<GesturePoint> points2 = new ArrayList<>();
		int i=0;
                //processing the points
		while(i < points.size())
                {
			GesturePoint p2 = points.get(i);
			double x1 = p2.x + pt.x - central.x;
			double y1 =p2.y + pt.y - central.y;
			GesturePoint p3 = new GesturePoint(x1, y1);
			points2.add(p3);
			i++;
		}
		return points2;
	}

        //helper method for further calculations

	public static double helper(ArrayList<GesturePoint> points, double val1, double val2, double val3, GestureTemplate template)
        {
		double x1 = CanvasBoard.phi * val1 + (1.0 - CanvasBoard.phi) * val2;
		ArrayList<GesturePoint> points2 = new ArrayList<>();
		points2 = rotation(points, x1);
		double d = 0.0;

		for(int i=0; i<points2.size(); i++)
                {
			GesturePoint a = points2.get(i);
			GesturePoint b = template.points.get(i);
			double dx = b.x - a.x;
			double dy = b.y - a.y;
			d += Math.sqrt(dx*dx + dy*dy);
		}

		double v1 = d / (double)points2.size();
		double x2 = (1.0 - CanvasBoard.phi) * val1 + CanvasBoard.phi * val2;
		points2 = rotation(points, x2);
		d = 0.0;
                //processing the points
		for(int i=0; i<points2.size(); i++)
                {
			GesturePoint a = points2.get(i);
			GesturePoint b = template.points.get(i);
			double dx = b.x - a.x;
			double dy = b.y - a.y;
			d += Math.sqrt(dx*dx + dy*dy);
		}

		double v2= d / (double)points2.size();
                //condition to check absolute distance
		while (Math.abs(val2- val1) > val3)
		{
			if (v1 < v2)
                        {
				val2 = x2;
				x2 = x1;
				v2 = v1;
				x1 = CanvasBoard.phi * val1 + (1.0 - CanvasBoard.phi) * val2;
				points2 = rotation(points, x1);
				d = 0.0;
				for(int i=0; i<points2.size(); i++) {
					GesturePoint a = points2.get(i);
					GesturePoint b = template.points.get(i);
					double dx = b.x - a.x;
					double dy = b.y - a.y;
					d += Math.sqrt(dx*dx + dy*dy);
				}
				v1 = d / (double)points2.size();
			}
                        else
                        {
				val1 = x1;
				x1 = x2;
				v1 = v2;
				x2 = (1.0 - CanvasBoard.phi) * val1 + CanvasBoard.phi * val2;
				points2 = rotation(points2, x2);
				d = 0.0;
				for(int i=0; i<points2.size(); i++)
                                {
					GesturePoint a = points2.get(i);
					GesturePoint b = template.points.get(i);
					double dx = b.x - a.x;
					double dy = b.y - a.y;
					d += Math.sqrt(dx*dx + dy*dy);
				}
				v2 = d / (double)points2.size();
			}
		}
		return Math.min(v1, v2);
	}


}
