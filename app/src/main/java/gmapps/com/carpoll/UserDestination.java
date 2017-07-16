package gmapps.com.carpoll;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.math.BigInteger;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserDestination.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserDestination#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserDestination extends Fragment implements PlaceSelectionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RadioButton radioButton;
    private OnFragmentInteractionListener mListener;
    private String placeId;
    private LatLng placeLatLang;
    private boolean placeSelect=false;
    public UserDestination() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserDestination.
     */
    // TODO: Rename and change types and number of parameters
    public static UserDestination newInstance(String param1, String param2) {
        UserDestination fragment = new UserDestination();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_user_destination, container, false);
        Button continueBtn=(Button) v.findViewById(R.id.continueBtn);
        final RadioGroup typeofCustomer=(RadioGroup) v.findViewById(R.id.typeofCustomer);
        PlaceAutocompleteFragment placeAutocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        placeAutocompleteFragment.setOnPlaceSelectedListener(this);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get selected radio button from radioGroup
                int selectedId = typeofCustomer.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                if (typeofCustomer.getCheckedRadioButtonId() != -1) {
                    int id = typeofCustomer.getCheckedRadioButtonId();
                    View radioButton = typeofCustomer.findViewById(id);
                    int radioId = typeofCustomer.indexOfChild(radioButton);
                    RadioButton btn = (RadioButton) typeofCustomer.getChildAt(radioId);
                    String selection = "" + btn.getText();
                    if(placeSelect){
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = user.getUid();
                        Intent i=new Intent(getActivity(),MapsActivity.class);
                        i.putExtra("id",uid);
                        i.putExtra("placeId",placeId);
                        i.putExtra("typeUser",selection);
                        i.putExtra("latLang",placeLatLang.toString());
                        startActivity(i);
                    }
                    else{
                        Toast.makeText(getContext(),"Please select Destination Place",Toast.LENGTH_SHORT).show();
                    }

                }




            }
        });
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPlaceSelected(Place place) {
        placeSelect=true;
        Log.i("place", "Place Selected: " + place.getName());
        Log.i("place", "Place Selected: " + place.getId());
        placeId=place.getId();
        placeLatLang=place.getLatLng();
    }

    @Override
    public void onError(Status status) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
