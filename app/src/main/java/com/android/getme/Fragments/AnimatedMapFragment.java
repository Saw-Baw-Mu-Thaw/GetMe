package com.android.getme.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.getme.Activities.ChooseVehicleActivity;
import com.android.getme.Listeners.TrackRideListener;
import com.android.getme.R;

import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.milestones.MilestoneBitmapDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneLineDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneManager;
import org.osmdroid.views.overlay.milestones.MilestoneMeterDistanceLister;
import org.osmdroid.views.overlay.milestones.MilestoneMeterDistanceSliceLister;
import org.osmdroid.views.overlay.milestones.MilestonePathDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneVertexLister;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AnimatedMapFragment extends Fragment {


    private  static final float LINE_WIDTH_BIG = 12;
    private static final float TEXT_SIZE = 20;
    private static final int COLOR_POLYLINE_STATIC = Color.BLUE;
    private static final int COLOR_POLYLINE_ANIMATED = Color.GREEN;
    private static final int COLOR_BACKGROUND = Color.WHITE;

    private double mAnimatedMetersSoFar;
    private boolean mAnimationEnded;


    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;
    private int duration = 5000;

    private List<GeoPoint> mGeoPoints;
    private double distance;

    private String userAgent;
    private MapView map;
    private TrackRideListener listener;

    public AnimatedMapFragment() {

    }

    public static AnimatedMapFragment newInstance(double startLat, double startLng, double endLat, double endLng) {
        AnimatedMapFragment fragment = new AnimatedMapFragment();
        Bundle args = new Bundle();
        args.putDouble("startLat", startLat);
        args.putDouble("startLng", startLng);
        args.putDouble("endLat", endLat);
        args.putDouble("endLng", endLng);
        fragment.setArguments(args);
        return fragment;
    }

    public static AnimatedMapFragment newInstance(double startLat, double startLng, double endLat, double endLng, int duration) {
        AnimatedMapFragment fragment = new AnimatedMapFragment();
        Bundle args = new Bundle();
        args.putDouble("startLat", startLat);
        args.putDouble("startLng", startLng);
        args.putDouble("endLat", endLat);
        args.putDouble("endLng", endLng);
        args.putInt("duration", duration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle b = getArguments();
            startLat = b.getDouble("startLat");
            startLng = b.getDouble("startLng");
            endLat = b.getDouble("endLat");
            endLng = b.getDouble("endLng");
            duration = b.getInt("duration", 5000);
        }

        if(getContext() instanceof TrackRideListener) {
            listener = (TrackRideListener) getContext();
        }

        userAgent = ActivityCompat.getString(getContext(), R.string.GetMe_OSM_User_Agent);
        Configuration.getInstance().setUserAgentValue(userAgent);

        setGeoPoints();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_animated_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        map = view.findViewById(R.id.map);
    }

    private void addOverlays() {
        final Polyline polyline =new Polyline(map);
        polyline.getOutlinePaint().setColor(COLOR_POLYLINE_STATIC);
        polyline.getOutlinePaint().setStrokeWidth(LINE_WIDTH_BIG);
        polyline.setPoints(mGeoPoints);
        polyline.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);

        final List<MilestoneManager> managers = new ArrayList<>();
        final MilestoneMeterDistanceSliceLister slicerForPath =new MilestoneMeterDistanceSliceLister();
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        final MilestoneMeterDistanceSliceLister slicerForIcon =new MilestoneMeterDistanceSliceLister();

        managers.add(getAnimatedPathManager(slicerForPath));
        managers.add(getAnimatedIconManager(slicerForIcon, bitmap));
        managers.add(getHalfKilometerManager());
        managers.add(getKilometerManager());
        managers.add(getStartManager(bitmap));
        managers.add(getEndManager(bitmap));

        polyline.setMilestoneManagers(managers);
        map.getOverlayManager().add(polyline);
        final ValueAnimator percentageComplete = ValueAnimator.ofFloat(0, (int)Math.round(distance * 1000));
        percentageComplete.setDuration(duration);
        percentageComplete.setStartDelay(1000);
        percentageComplete.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                mAnimatedMetersSoFar = (float)valueAnimator.getAnimatedValue();
                slicerForPath.setMeterDistanceSlice(0, mAnimatedMetersSoFar);
                slicerForIcon.setMeterDistanceSlice(mAnimatedMetersSoFar, mAnimatedMetersSoFar);
                map.invalidate();
            }
        });

        percentageComplete.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationEnded = true;
                Marker car = new Marker(map);
                car.setPosition(mGeoPoints.get(mGeoPoints.size()-1));
                car.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.car));
                map.getOverlays().add(car);
                map.invalidate();


                if (listener != null) {
                    listener.OnArrivalAnimationCompleted();
                }

            }
        });

        percentageComplete.start();
    }

    private  void setGeoPoints() {
        String provider = ActivityCompat.getString(requireContext(), R.string.provider);

        RoadManager roadManager;
        if(provider.equals("OSRM")) {
            roadManager = new OSRMRoadManager(requireContext(), userAgent);
            ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_CAR);
        } else {
            String api_key = ActivityCompat.getString(requireContext(), R.string.GH_key);
            roadManager = new GraphHopperRoadManager(api_key, false);
            roadManager.addRequestOption("profile=car");
            roadManager.addRequestOption("snap_prevention=motorway");
            roadManager.addRequestOption("snap_prevention=ferry");
            roadManager.addRequestOption("snap_prevention=tunnel");
            roadManager.addRequestOption("locale=en");
        }
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(new GeoPoint(startLat, startLng));
        waypoints.add(new GeoPoint(endLat, endLng));

        MyAsyncTask asyncTask = new MyAsyncTask();
        asyncTask.execute(roadManager, waypoints);
    }

    class MyAsyncTask extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected Object[] doInBackground(Object... objects) {
            RoadManager roadManager = (RoadManager) objects[0];
            ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>) objects[1];

            Road road = roadManager.getRoad(waypoints);
            ArrayList<GeoPoint> geoPoints = new ArrayList<>();

            for (int i = 0; i < road.mNodes.size(); i++) {
                RoadNode node = road.mNodes.get(i);

                geoPoints.add(node.mLocation);
            }

            Object[] results = new Object[3];
            results[0] = geoPoints;
            results[1] = Double.valueOf(road.mLength);
            results[2] = (int) road.mDuration;
            return results;
        }

        @Override
        protected void onPostExecute(Object[] objects) {
            super.onPostExecute(objects);
            mGeoPoints = (ArrayList<GeoPoint>)objects[0];
            distance = (Double) objects[1];
            listener.setDuration((Integer) objects[2]);
            final BoundingBox boundingBox = BoundingBox.fromGeoPoints(mGeoPoints);
            map.zoomToBoundingBox(boundingBox, false, 30);
            map.setMultiTouchControls(true);
            addOverlays();
        }
    }

    private MilestoneManager getEndManager(Bitmap bitmap) {
        return new MilestoneManager(
                new MilestoneMeterDistanceLister((int)Math.round(distance * 1000)),
                new MilestoneBitmapDisplayer(0, true,
                        bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2) {
                    @Override
                    protected void draw(Canvas pCanvas, Object pParameter) {

                        if (mAnimationEnded) {
                            super.draw(pCanvas, pParameter);
                        }
                    }
                }
        );
    }

    private MilestoneManager getAnimatedPathManager(MilestoneMeterDistanceSliceLister slicerForPath) {
        final Paint slicePaint = getStrokePaint(COLOR_POLYLINE_ANIMATED, LINE_WIDTH_BIG);
        return new MilestoneManager(slicerForPath, new MilestoneLineDisplayer(slicePaint));
    }

    private Paint getStrokePaint(int colorPolylineAnimated, float lineWidthBig) {
        final Paint paint = new Paint();
        paint.setStrokeWidth(lineWidthBig);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(colorPolylineAnimated);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    private MilestoneManager getAnimatedIconManager(MilestoneMeterDistanceSliceLister slicerForIcon, Bitmap bitmap) {
        return new MilestoneManager(
                slicerForIcon,
                new MilestoneBitmapDisplayer(0, true, bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2) {
                    @Override
                    protected void draw(Canvas pCanvas, Object pParameter) {
                        if(!mAnimationEnded) {
                            super.draw(pCanvas, pParameter);
                        } else {
                            super.draw(pCanvas, pParameter);
                        }

                    }
                }
        );
    }

    private MilestoneManager getHalfKilometerManager() {
        final Path arrowPath = new Path();
        arrowPath.moveTo(-5,-5);
        arrowPath.lineTo(5,0);
        arrowPath.lineTo(-5,5);
        arrowPath.close();
        final Paint backgroundPaint = getFillPaint(COLOR_BACKGROUND);
        return new MilestoneManager(
                new MilestoneMeterDistanceLister(500),
                new MilestonePathDisplayer(0, true, arrowPath,backgroundPaint) {
                    @Override
                    protected void draw(Canvas pCanvas, Object pParameter) {
                        final int halfKilometers = (int) Math.round(((double) pParameter/500));
                        if(halfKilometers % 2 == 0) {
                            return;
                        }
                        super.draw(pCanvas, pParameter);
                    }
                }
        );
    }

    private Paint getFillPaint(int colorBackground) {
        final Paint paint = new Paint();
        paint.setColor(colorBackground);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        return paint;
    }

    private MilestoneManager getKilometerManager() {
        final float backgroundRadius = 20;
        final Paint backgroundPaint1 = getFillPaint(COLOR_BACKGROUND);
        final Paint backgroundPaint2 = getFillPaint(COLOR_POLYLINE_ANIMATED);
        final Paint textPaint1 = getTextPaint(COLOR_POLYLINE_STATIC);
        final Paint textPaint2 = getTextPaint(COLOR_BACKGROUND);
        final Paint borderPaint = getStrokePaint(COLOR_BACKGROUND, 2);
        return new MilestoneManager(
                new MilestoneMeterDistanceLister(1000),
                new MilestoneDisplayer(0, false) {
                    @Override
                    protected void draw(Canvas pCanvas, Object pParameter) {
                        final double meters = (double) pParameter;
                        final int kilometers = (int) Math.round(meters/1000);
                        final boolean checked = meters < mAnimatedMetersSoFar || (kilometers == 10 && mAnimationEnded);
                        final Paint textpaint = checked ? textPaint2 : textPaint1;
                        final Paint backgroundPaint = checked ? backgroundPaint2 : backgroundPaint1;
                        final String text = "" + kilometers + "K";
                        final Rect rect = new Rect();
                        textPaint1.getTextBounds(text, 0, text.length(), rect);
                        pCanvas.drawCircle(0,0, backgroundRadius, backgroundPaint);
                        pCanvas.drawText(text, -rect.left - rect.width() / 2, rect.height()/2 - rect.bottom, textpaint);
                        pCanvas.drawCircle(0,0, backgroundRadius + 1, borderPaint);
                    }
                }
        );
    }

    private Paint getTextPaint(int colorPolylineStatic) {
        final Paint paint = new Paint();
        paint.setColor(colorPolylineStatic);
        paint.setTextSize(TEXT_SIZE);
        paint.setAntiAlias(true);
        return paint;
    }

    private MilestoneManager getStartManager(Bitmap bitmap) {
        return new MilestoneManager(
                new MilestoneVertexLister(),
                new MilestoneBitmapDisplayer(0, true,
                        bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2) {
                    @Override
                    protected void draw(Canvas pCanvas, Object pParameter) {
                        if(0 != (int) pParameter) {
                            return;
                        }
                        if(mAnimatedMetersSoFar > 0) {
                            return;
                        }
                        super.draw(pCanvas, pParameter);
                    }
                }
        );
    }
}