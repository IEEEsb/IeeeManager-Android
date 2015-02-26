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

public class SettingsAdapter extends ArrayAdapter<User> {

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

	public SettingsAdapter(Context context, int resource) {
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
			boolean registered = !doorUser.getToken().equals("");
			if (registered) {

				rowView = inflater.inflate(
						R.layout.settings_user_data_card_registered, parent,
						false);

				TextView tv = ((TextView) rowView.findViewById(R.id.nameLabel));
				tv.setText(tv.getText() + doorUser.getName());
				tv = ((TextView) rowView.findViewById(R.id.dniLabel));
				tv.setText(tv.getText() + doorUser.getDni());
				tv = ((TextView) rowView.findViewById(R.id.tokenLabel));
				tv.setText(tv.getText() + doorUser.getToken());
			} else {

				rowView = inflater.inflate(
						R.layout.settings_user_data_card_unregistered, parent,
						false);

				if (!doorUser.getName().equals(""))
					((EditText) rowView.findViewById(R.id.name))
							.setText(doorUser.getName());
				if (!doorUser.getDni().equals(""))
					((EditText) rowView.findViewById(R.id.dni))
							.setText(doorUser.getDni());
				if (!doorUser.getRegisterId().equals(""))
					((EditText) rowView.findViewById(R.id.registrationId))
							.setText(doorUser.getRegisterId());
			}

			break;
		
		case TYPE_SLIC3R:
			Slic3rUser user=(Slic3rUser) u;
			if (convertView == null)
				rowView = inflater.inflate(R.layout.settings_user_email_card, parent, false);
			else
				rowView = convertView;
			EditText field=(EditText) rowView.findViewById(R.id.emailField);
			field.setText(user.getEmail());
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
						&& !animations.get(position).hasEnded())
					rowView.setAnimation(animations.get(position));

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
