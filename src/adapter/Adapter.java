package adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.awisconsinbestiary.android.R;

import database.Entry;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class Adapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	ImageView imageView;
	LruCache<String,Bitmap> bitmapCache;
	AQuery aq;


	public Adapter(Activity a, ArrayList<HashMap<String, String>> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory()); 
        
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        
         bitmapCache = new LruCache<String,Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            
        }};
        
        		
	}



	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        bitmapCache.put(key, bitmap);
	    }
	}
	
	public Bitmap getBitmapFromMemCache(String key) {
	    return bitmapCache.get(key);
	}
	

	public View getView(final int position, View convertView, ViewGroup parent) {


		View vi = convertView;
		if (convertView == null)
		{
			vi = inflater.inflate(R.layout.list_view_item, null);
		}
		
		


		TextView header = (TextView) vi.findViewById(R.id.header);
		TextView secondary = (TextView) vi.findViewById(R.id.secondLine);
		imageView = (ImageView) vi.findViewById(R.id.icon);

		HashMap<String, String> e = new HashMap<String, String>();
		e = data.get(position);
		header.setText(e.get("currentTime"));
		secondary.setText(e.get("groupOrPhyla"));
		
		if (getBitmapFromMemCache(Integer.valueOf(position)+"") != null) {
			imageView.setImageBitmap(getBitmapFromMemCache(Integer.valueOf(position)+""));
		}
		else
		{
			final Matrix matrix = new Matrix();
			if(e.get("photo") != null)
			{
				//Depending on the orientation of the phone when the image was taken, the image can be displayed 
				//sideways in the image view. This will rotate the image correctly
		        try {
					ExifInterface exif = new ExifInterface(e.get("photo"));
		            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		            matrix.setScale(.8f, .8f);
		            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
		                matrix.postRotate(90);
		            }
		            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
		                matrix.postRotate(180);
		            }
		            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
		                
		                matrix.postRotate(270);
		            }

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			

			// bimatp factory
			final BitmapFactory.Options options = new BitmapFactory.Options();

			// downsizing image as it throws OutOfMemory Exception for
			// larger
			// images
			options.inSampleSize = 8;
			

			aq = new AQuery(convertView);
			if (e.get("photo") != null) {
				//load image from file with callback
				aq.id(R.id.icon).image(new File(e.get("photo")), false, 50, new BitmapAjaxCallback(){

				    @Override
				    public void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status){
						bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true); // rotating bitmap
				        iv.setImageBitmap(bm);
				        notifyDataSetChanged();
				        bitmapCache.put(Integer.valueOf(position)+"", bm);

				        
				    }
				    
				});


			}

		}
		
		return vi;
	}
}