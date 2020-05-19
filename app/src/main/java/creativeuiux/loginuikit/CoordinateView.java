package creativeuiux.loginuikit;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CoordinateView extends ViewModel {

    private MutableLiveData<Double> Latitude;
    private MutableLiveData<Double> Longitude;



    public MutableLiveData<Double> getLatitude(){

         if (Latitude==null){

             Latitude=new MutableLiveData<>();
             Latitude.setValue(0.0);
         }


        return  Latitude;
    }


    public MutableLiveData<Double> getLongitude(){

        if (Longitude==null){

            Longitude=new MutableLiveData<>();
            Longitude.setValue(0.0);
        }



        return Longitude;
    }


public  void setLatitude(Double d){

        if (Latitude.getValue()!=null){
            Latitude.setValue(d);

        }
    }

    public  void setLongitude(Double d){

        if (Longitude.getValue() != null) {
            Longitude.setValue(d);

        }
    }










}
