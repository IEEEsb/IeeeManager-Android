package es.ieeesb.ieeemanager.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ieeedooropener.R;

import es.ieeesb.ieeemanager.model.DoorUser;
import es.ieeesb.ieeemanager.model.FridgeUser;
import es.ieeesb.ieeemanager.model.LatchUser;
import es.ieeesb.ieeemanager.model.Printer3dUser;
import es.ieeesb.ieeemanager.model.Slic3rUser;
import es.ieeesb.ieeemanager.model.User;

public class CardAdapter extends ArrayAdapter<User> {

    public static final int TYPE_PUERTA = 0;
    public static final int TYPE_NEVERA = 1;
    public static final int TYPE_PRUSA = 2;
    public static final int TYPE_LATCH = 3;
    public static final int TYPE_SLIC3R = 4;
    public static final boolean ANIMATE = true;

    private final Context context;
    private final int resource;
    private ArrayList<Boolean> animatedViews;
    private ArrayList<Animation> animations;

    static class DoorViewHolder {
        TextView token;
    }

    static class FridgeViewHolder {
        TextView budgetText;
    }

    static class Printer3dViewHolder {
        TextView statusText;
        TextView fileText;
        TextView timeText;
        TextView progressText;
    }

    static class LatchViewHolder {
        EditText latchTokenField;
    }

    public CardAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.animatedViews = new ArrayList<Boolean>();
        this.animations = new ArrayList<Animation>();
    }

    @Override
    public void add(User object) {
        animatedViews.add(false);
        animations.add(null);
        super.add(object);
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public int getItemViewType(int position) {
        User u = getItem(position);
        int type = -1;
        if (u instanceof DoorUser)
            type = TYPE_PUERTA;
        if (u instanceof FridgeUser)
            type = TYPE_NEVERA;
        if (u instanceof Printer3dUser)
            type = TYPE_PRUSA;
        if (u instanceof LatchUser)
            type = TYPE_LATCH;
        if (u instanceof Slic3rUser)
            type = TYPE_SLIC3R;
        return type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        User u = getItem(position);
        View rowView = null;
        switch (getItemViewType(position)) {
            case TYPE_PUERTA:
                DoorUser doorUser = (DoorUser) u;
                if (convertView == null) {
                    rowView = inflater.inflate(R.layout.door_card_registered,
                            parent, false);
                    DoorViewHolder viewHolder = new DoorViewHolder();
                    viewHolder.token = (TextView) rowView
                            .findViewById(R.id.tokenText);
                    rowView.setTag(viewHolder);
                } else
                    rowView = convertView;
                DoorViewHolder doorVh = (DoorViewHolder) rowView.getTag();
                doorVh.token.setText(doorUser.getToken());
                break;
            case TYPE_NEVERA:
                FridgeUser user = (FridgeUser) u;
                if (convertView == null) {
                    rowView = inflater.inflate(R.layout.fridge_card, parent, false);
                    FridgeViewHolder viewHolder = new FridgeViewHolder();
                    viewHolder.budgetText = (TextView) rowView
                            .findViewById(R.id.budgetText);
                    rowView.setTag(viewHolder);
                } else
                    rowView = convertView;
                FridgeViewHolder fridgeVh = (FridgeViewHolder) rowView.getTag();
                if (user.getBudget() > Float.NEGATIVE_INFINITY) {
                    fridgeVh.budgetText.setText(user.getBudget() + "€");
                } else {
                    fridgeVh.budgetText.setText(R.string.no_budget);
                }
                break;
            case TYPE_PRUSA:
                Printer3dUser printerUser = (Printer3dUser) u;
                if (convertView == null) {
                    rowView = inflater.inflate(R.layout.printer3d_card, parent,
                            false);
                    Printer3dViewHolder viewHolder = new Printer3dViewHolder();
                    viewHolder.statusText = (TextView) rowView.findViewById(R.id.statusText);
                    viewHolder.fileText = (TextView) rowView.findViewById(R.id.fileText);
                    viewHolder.progressText = (TextView) rowView.findViewById(R.id.progressText);
                    viewHolder.timeText = (TextView) rowView.findViewById(R.id.timeText);
                    rowView.setTag(viewHolder);
                } else
                    rowView = convertView;
                Printer3dViewHolder printer3dVh = (Printer3dViewHolder) rowView.getTag();
                printer3dVh.statusText.setText("Estado: " + printerUser.getStatus());
                printer3dVh.fileText.setText("Archivo: " + printerUser.getFile());
                printer3dVh.timeText.setText("Tiempo restante: " + printerUser.getTime() / 3600
                        + "h" + (printerUser.getTime() % 3600) / 60 + "m"
                        + (printerUser.getTime() % 60) + "s");
                printer3dVh.progressText.setText("Progreso: " + printerUser.getProgress() + "%");
                break;
            case TYPE_LATCH:
                LatchUser latchUser = (LatchUser) u;
                if (convertView == null) {
                    rowView = inflater.inflate(R.layout.latch_card, parent, false);
                    LatchViewHolder viewHolder = new LatchViewHolder();
                    viewHolder.latchTokenField = (EditText) rowView
                            .findViewById(R.id.pairingTokenField);
                    rowView.setTag(viewHolder);
                } else
                    rowView = convertView;
                LatchViewHolder latchVh = (LatchViewHolder) rowView.getTag();
                EditText ed = latchVh.latchTokenField;
                ed.setText(latchUser.getPairingToken());
                if (latchUser.isPaired()) {
                    ed.setEnabled(false);
                }
                break;
            case TYPE_SLIC3R:
                if (convertView == null)
                    rowView = inflater.inflate(R.layout.slic3r_card, parent, false);
                else
                    rowView = convertView;
                break;
            default:
                rowView = inflater.inflate(resource, parent, false);
                break;
        }
        if (ANIMATE) {
            if (!animatedViews.get(position)) {
                animatedViews.set(position, true);
                animateCard(position, rowView);
            } else {
                if (animations.get(position) != null
                        && !animations.get(position).hasEnded()) {
                    rowView.setAnimation(animations.get(position));
                }

            }
        }

        return rowView;
    }

    public void animateCard(int position, View v) {
        v.setVisibility(View.INVISIBLE);
        Animation animation = AnimationUtils
                .loadAnimation(context, R.anim.move);
        animation.setDuration(350 + position * 100);
        animation.setStartOffset(500);
        v.startAnimation(animation);
        animations.set(position, animation);
    }

    public void resetAnimations() {
        for (int i = 0; i < animatedViews.size(); i++) {
            animatedViews.set(i, false);
        }
    }

    public void disableAnimations() {
        for (int i = 0; i < animatedViews.size(); i++) {
            animatedViews.set(i, true);
        }
    }

}
