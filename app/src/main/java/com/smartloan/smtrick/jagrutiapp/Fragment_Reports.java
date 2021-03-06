package com.smartloan.smtrick.jagrutiapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static com.smartloan.smtrick.jagrutiapp.Constants.CALANDER_DATE_FORMATE;
import static com.smartloan.smtrick.jagrutiapp.Constants.GLOBAL_DATE_FORMATE;


public class Fragment_Reports extends Fragment

{
    int fromYear, fromMonth, fromDay;
    int toYear, toMonth, toDay;
    long fromDate, toDate;
    private RecyclerView catalogRecycler;
    private DatabaseReference mdataRefpatient;
    private List<Reportmodel> catalogList;
    private ProgressBar catalogprogress;
    private RecyclerView.Adapter adapter;
   EditText edittextfromdate,edittexttodate;
    ReportAdapter reportadapter;
    Reportmodel reportsmodel;
    ArrayList<Reportmodel> leedsModelArrayList;
    Button total, Excel;
   // int[] animationList = {R.anim.layout_animation_up_to_down};
    int i = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewreports, container, false);

       // getActivity().getActionBar().setTitle("Products");
        //catalogprogress = (ProgressBar) view.findViewById(R.id.catalog_progress);
        catalogRecycler = (RecyclerView) view.findViewById(R.id.catalog_recycle);
        edittextfromdate = (EditText) view.findViewById(R.id.edittextfromdate);
        edittexttodate = (EditText) view.findViewById(R.id.edittexttodate);

        total = (Button) view.findViewById(R.id.butonsubmit);
        Excel = (Button) view.findViewById(R.id.butonXL);
        catalogList = new ArrayList<>();
        setFromDateClickListner();
        setToDateClickListner();

      //  catalogprogress.setVisibility(View.VISIBLE);
        mdataRefpatient = FirebaseDatabase.getInstance().getReference("Patients");
        mdataRefpatient.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot mainproductSnapshot : dataSnapshot.getChildren()) {

                    Reportmodel mainProducts = mainproductSnapshot.getValue(Reportmodel.class);
                    catalogList.add(mainProducts);
                }

                leedsModelArrayList = (ArrayList<Reportmodel>) catalogList;
                adapter = new ReportAdapter(getActivity(), catalogList);

                //adding adapter to recyclerview
                catalogRecycler.setAdapter(adapter);
               // CatalogAdapter catalogAdapter = new CatalogAdapter(catalogList);
                catalogRecycler.setHasFixedSize(true);
                catalogRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                //catalogRecycler.setAdapter(catalogAdapter);
               // catalogprogress.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File sd = Environment.getExternalStorageDirectory();
                String csvFile = "myData.xls";

                File directory = new File(sd.getAbsolutePath());
                //create directory if not exist
                if (!directory.isDirectory()) {
                    directory.mkdirs();
                }
                try {

                    //file path
                    File file = new File(directory, csvFile);
                    WorkbookSettings wbSettings = new WorkbookSettings();
                    wbSettings.setLocale(new Locale("en", "EN"));
                    WritableWorkbook workbook;
                    workbook = Workbook.createWorkbook(file, wbSettings);
                    //Excel sheet name. 0 represents first sheet
                    WritableSheet sheet = workbook.createSheet("userList", 0);

                    sheet.addCell(new Label(0, 0, "Id")); // column and row
                    sheet.addCell(new Label(1, 0, "Name"));
                    sheet.addCell(new Label(2, 0, "Doctor Name"));
                    sheet.addCell(new Label(3, 0, "Date"));
                    sheet.addCell(new Label(4, 0, "Disease"));

                    for (int i = 0; i<leedsModelArrayList.size(); i++) {
                        String id = leedsModelArrayList.get(i).getPatientId();
                        String name = leedsModelArrayList.get(i).getPname();
                        String doctor = leedsModelArrayList.get(i).getCdoctor();
                        String date = (String) Utility.convertMilliSecondsToFormatedDate(leedsModelArrayList.get(i).getCreatedDateTimeLong(), GLOBAL_DATE_FORMATE);
                        String disease = leedsModelArrayList.get(i).getDiagnosys();

                        sheet.addCell(new Label(0, i+1, id));
                        sheet.addCell(new Label(1, i+1, name));
                        sheet.addCell(new Label(2, i+1, doctor));
                        sheet.addCell(new Label(3, i+1, date));
                        sheet.addCell(new Label(4, i+1, disease));
                    }

                    workbook.write();
                    workbook.close();
                    Toast.makeText(getContext(), "Data Exported in a Excel Sheet", Toast.LENGTH_SHORT).show();

                    Intent myIntent = new Intent(Intent.ACTION_VIEW);
                    myIntent.setData(Uri.fromFile(new File(csvFile)));
                    Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                    startActivity(j);

                } catch(Exception e){
                    e.printStackTrace();
                }

            }
        });


        return view;
}

    private void setFromCurrentDate() {
        Calendar mcurrentDate = Calendar.getInstance();
        fromYear = mcurrentDate.get(Calendar.YEAR);
        fromMonth = mcurrentDate.get(Calendar.MONTH);
        fromDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
    }

    private void setFromDateClickListner() {
        setFromCurrentDate();
        edittextfromdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR, selectedyear);
                        myCalendar.set(Calendar.MONTH, selectedmonth);
                        myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);
                        SimpleDateFormat sdf = new SimpleDateFormat(CALANDER_DATE_FORMATE, Locale.FRANCE);
                        String formatedDate = sdf.format(myCalendar.getTime());
                        edittextfromdate.setText(formatedDate);
                        fromDay = selectedday;
                        fromMonth = selectedmonth;
                        fromYear = selectedyear;
                        fromDate = Utility.convertFormatedDateToMilliSeconds(formatedDate, CALANDER_DATE_FORMATE);
                         filterByDate(leedsModelArrayList);
                    }
                }, fromYear, fromMonth, fromDay);
                mDatePicker.show();
            }
        });
    }

    private void setToCurrentDate() {
        toDate = System.currentTimeMillis();
        Calendar mcurrentDate = Calendar.getInstance();
        toYear = mcurrentDate.get(Calendar.YEAR);
        toMonth = mcurrentDate.get(Calendar.MONTH);
        toDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
    }

    private void setToDateClickListner() {
        setToCurrentDate();
        edittexttodate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR, selectedyear);
                        myCalendar.set(Calendar.MONTH, selectedmonth);
                        myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);
                        SimpleDateFormat sdf = new SimpleDateFormat(CALANDER_DATE_FORMATE, Locale.FRANCE);
                        edittexttodate.setText(sdf.format(myCalendar.getTime()));
                        toDay = selectedday;
                        toMonth = selectedmonth;
                        toYear = selectedyear;
                        toDate = myCalendar.getTimeInMillis();
                       filterByDate(leedsModelArrayList);
                    }
                }, toYear, toMonth, toDay);
                mDatePicker.show();
            }
        });
    }


    private void filterByDate(ArrayList<Reportmodel> leedsModelArrayList) {
        try {
            ArrayList<Reportmodel> filterArrayList = new ArrayList<>();
            if (leedsModelArrayList != null) {
                if (fromDate > 0) {
                    for (Reportmodel leedsModel : leedsModelArrayList) {
                        if (leedsModel.getCreatedDateTimeLong() >= fromDate && leedsModel.getCreatedDateTimeLong() <= toDate) {
                            filterArrayList.add(leedsModel);
                        }
                    }
                } else {
                    for (Reportmodel leedsModel : leedsModelArrayList) {
                        if (leedsModel.getCreatedDateTimeLong() <= toDate) {
                            filterArrayList.add(leedsModel);
                        }
                    }
                }
            }
            serAdapter(filterArrayList);
        } catch (Exception e) {
            ExceptionUtil.logException(e);
        }
    }

    private void serAdapter(ArrayList<Reportmodel> reportmodels) {
      setReports(reportmodels);
        if (reportmodels != null) {
            if (reportadapter == null) {
                reportadapter = new ReportAdapter(getActivity(),reportmodels);
                catalogRecycler.setAdapter(reportadapter);
                //onClickListner();
            } else {
                ArrayList<Reportmodel> leedsModelArrayList = new ArrayList<>();
                leedsModelArrayList.addAll(reportmodels);
                reportadapter.reload(leedsModelArrayList);
                catalogRecycler.setAdapter(reportadapter);
            }
        }
    }

    private void setReports(ArrayList<Reportmodel> leedsModelArrayList) {
        int approvedCount = 0, rejectedCount = 0;
        double totalPayout = 0;
        if (leedsModelArrayList != null) {
            for (Reportmodel leedsModel :
                    leedsModelArrayList) {


                approvedCount++;
            }//end of for

         total.setText(String.valueOf(approvedCount));
            // fragmentReportBinding.textViewPayoutAmount.setText(String.valueOf(totalPayout));
        } else {

         total.setText("Total : 0");
            //fragmentReportBinding.textViewPayoutAmount.setText("0.0");
        }
    }



}
