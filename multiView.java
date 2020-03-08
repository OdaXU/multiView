import java.awt.desktop.SystemSleepEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class multiView {
    static ArrayList<double[]> data_xa = new ArrayList<>();
    static ArrayList<double[]> data_xb = new ArrayList<>();
    static ArrayList<Double> data_y = new ArrayList<>();
    static ArrayList<double[]> result_arr = new ArrayList<>();
    static double max_acc = 0;
    static double acc_r = 0;
    static double acc_c = 0;
    static double acc_a = 0;
    public static int tp_tol = 0;
    public static int fp_tol = 0;
    public static int tn_tol = 0;
    public static int fn_tol = 0;
    String[] strArr;
    double[] data_tmp;
    int times = 500;
    static Random random;
    double[] grad_xa;
    double[] grad_xb;
    double learn_rate = 0.001;
    public static void main(String[] args){
        System.out.println("start");
        multiView pro = new multiView();
        pro.tryAllDatabase();
//        pro.inputData("/Users/Oda/Documents/multiview/data/awaperclass100_map/c1a.csv",0);
//        pro.inputData("/Users/Oda/Documents/multiview/data/awaperclass100_map/c1b.csv",1);
//        multiView.random = new Random();
//        randomData data = randomList(data_xa,data_xb,data_y);
//        ArrayList<double[]> random_xa = data.random_xa;
//        ArrayList<double[]> random_xb = data.random_xb;
//        ArrayList<Double> random_y = data.random_y;
//        pro.find_para(random_xa,random_xb,random_y,0.5,0.5,0.5,0.1,0.1,0.1);
//        //multiView.saveInCsv("/Users/Oda/IdeaProjects/multiView/src/multiView.java");
    }

    public void tryAllDatabase(){
        String path0 = "/Users/Oda/Documents/multiview/data/awaperclass100_map/";
        String path;
        for (int i = 1;i<=45;i++){
            max_acc = 0;
            acc_a = 99;
            acc_c = 99;
            acc_r = 99;
            //读入数据
            path = path0 + "c" + i + "a.csv";
            this.inputData(path,0);
            path = path0 + "c" + i + "b.csv";
            this.inputData(path,1);
            //打乱数据集
            multiView.random = new Random();
            randomData data = randomList(data_xa,data_xb,data_y);
            ArrayList<double[]> random_xa = data.random_xa;
            ArrayList<double[]> random_xb = data.random_xb;
            ArrayList<Double> random_y = data.random_y;
            //栅栏法参数寻优
            this.find_para(random_xa,random_xb,random_y,0.5,0.5,-1.7,0.1,0.1,0.1);
            System.out.println(i+":"+max_acc);
            System.out.println(acc_r+"-"+acc_c+"-"+acc_a);
        }
    }

    //未完成方法：输出到excel
    public static void saveInCsv(String path) {
        //String[] headArr = new String[]{"r","c","a","TP","FP","TN","FN","ACC","COST"};
        File csvFile = null;
        BufferedWriter csvWriter = null;
        try {
            csvFile = new File("/Users/Oda/IdeaProjects/multiView/src/result.csv");
            File parent = csvFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            csvFile.createNewFile();
            csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "GB2312"), 1024);
            //csvWriter.write(String.join(",",headArr));
            //csvWriter.newLine();

            csvWriter.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static randomData randomList(ArrayList<double[]> source_data_xa,ArrayList<double[]>source_data_xb,ArrayList<Double> source_data_y){
        ArrayList<double[]> random_data_xa = new ArrayList<double[]>(source_data_xa.size());
        ArrayList<double[]> random_data_xb = new ArrayList<double[]>(source_data_xa.size());
        ArrayList<Double> random_data_y = new ArrayList<Double>(source_data_xa.size());
        do{
            int randomIndex = Math.abs( new Random( ).nextInt( source_data_xa.size() ) );
            random_data_xa.add( source_data_xa.remove( randomIndex ) );
            random_data_xb.add( source_data_xb.remove( randomIndex ) );
            random_data_y.add( source_data_y.remove( randomIndex ) );
        }while( source_data_xa.size( ) > 0 );
        randomData data = new randomData();
        data.random_xa = random_data_xa;
        data.random_xb = random_data_xb;
        data.random_y = random_data_y;
        return data;
    }

    public void find_para(ArrayList<double[]> xa,ArrayList<double[]> xb ,ArrayList<Double> y,
                          double r,double c,double a,double gap_r,double gap_c,double gap_a){
        int para_num = 5;
        //以参数r,c,a为中心值和对应间隔gap生成参数列表
        double[] r_arr = new double[para_num];
        double[] c_arr = new double[para_num];
        double[] a_arr = new double[para_num];
        for (int i =0;i<para_num;i++){
            r_arr[i] = r+(i-para_num/2)*gap_r;
            c_arr[i] = c+(i-para_num/2)*gap_c;
            a_arr[i] = a+(i-(int)para_num/2)*gap_a;
        }
//        System.out.println(Arrays.toString(a_arr));
        for (double r_para:r_arr){
            for (double c_para:c_arr){
                for (double a_para:a_arr){
                    //System.out.println(a_para);
                    this.crossvalidation(xa,xb,y,r_para,c_para,a_para);
                }
            }
        }
    }

    //交叉验证
    public void crossvalidation(ArrayList<double[]> xa,ArrayList<double[]> xb ,ArrayList<Double> y,
                                double r,double c,double a){
        //将xa,xb和y分成五块
        int size = xa.size()/5;
        ArrayList<ArrayList<double[]>> data_xa_arr = new ArrayList<>();     //数据块列表
        ArrayList<ArrayList<double[]>> data_xb_arr = new ArrayList<>();
        ArrayList<ArrayList<Double>> data_y_arr = new ArrayList<>();
        for (int i = 0;i<5;i++){
            data_xa_arr.add(new ArrayList<double[]>());
            data_xb_arr.add(new ArrayList<double[]>());
            data_y_arr.add(new ArrayList<Double>());
            for (int l = i*size;l<(i+1)*size;l++){
                data_xa_arr.get(i).add(xa.get(l));
                data_xb_arr.get(i).add(xb.get(l));
                data_y_arr.get(i).add(y.get(l));
            }
        }
        double acc_tol = 0;
        //每次取出4块作为训练集，1块作为测试集
        for (int i = 0;i<5;i++){
            ArrayList<double[]> train_xa = multiView.collectData(i,data_xa_arr);
            ArrayList<double[]> train_xb = multiView.collectData(i,data_xb_arr);
            ArrayList<Double> train_y = multiView.collectY(i,data_y_arr);
            ArrayList<double[]> test_xa = data_xa_arr.get(i);
            ArrayList<double[]> test_xb = data_xb_arr.get(i);
            ArrayList<Double> test_y = data_y_arr.get(i);
            ArrayList<double[]> w = this.SGD(train_xa,train_xb,train_y,r,c,a);
            acc_tol += multiView.acc(test_xa,test_xb,test_y,w);
        }
        double mean_acc = acc_tol/5;
        mean_acc = Math.round((mean_acc*10000))/(double)10000;
        if (mean_acc>max_acc){
            max_acc=mean_acc;
            acc_r = r;
            acc_c = c;
            acc_a = a;
        }
        double[] result = new double[8];
        result[0] = r;
        result[1] = c;
        result[2] = a;
        result[3] = tp_tol;
        result[4] = fp_tol;
        result[5] = tn_tol;
        result[6] = fn_tol;
        result[7] = mean_acc;
        //System.out.println(Arrays.toString(result));
        result_arr.add(result);
        tp_tol = 0;fp_tol=0;tn_tol=0;fn_tol=0;
    }

    public static double cost_val(ArrayList<double[]> w,double r,double c ,double a){
        double cost = 0;
        double[] wa = w.get(0);
        double[] wb = w.get(1);
        cost += Matrix.mulMatrix(wa,wa);
        cost += r*Matrix.mulMatrix(wb,wb);
        int batch_xa = data_xa.size();
        for (int i = 0;i<batch_xa;i++){
            //int randomNum = random.nextInt(train_xa.size());
            double[] i_xa = data_xa.get(i);
            double[] i_xb = data_xa.get(i);
            double i_y = data_y.get(i);
            double cesi_xa = (i_y * Matrix.mulMatrix(i_xa, wa)) - 1;
            double cesi_xb = (i_y * Matrix.mulMatrix(i_xb, wb)) - 1;
            cost += c*(Math.exp(a*(cesi_xa+cesi_xb))-a*(cesi_xa+cesi_xb)-1);
        }
        return cost;
    }

    public static ArrayList<double[]> collectData(int test_index,ArrayList<ArrayList<double[]>> data_arr){
        ArrayList<double[]> train_data = new ArrayList<>();
        for (int i = 0;i<5;i++){
            if (i!=test_index){
                for (int k = 0;k<data_arr.get(i).size();k++){
                    train_data.add(data_arr.get(i).get(k));
                }
            }
        }
        return train_data;
    }
    public static ArrayList<Double> collectY(int test_index,ArrayList<ArrayList<Double>> data_arr){
        ArrayList<Double> train_data = new ArrayList<>();
        for (int i = 0;i<5;i++){
            if (i!=test_index){
                for (int k = 0;k<data_arr.get(i).size();k++){
                    train_data.add(data_arr.get(i).get(k));
                }
            }
        }
        return train_data;
    }

    public void inputData(String filePath,int wa_or_wb){
        File inFile = new File(filePath);
        String inString = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            int no = 0;
            while ((inString = reader.readLine())!=null){
                strArr = inString.split(",");
                data_tmp = new double[strArr.length];
                if (wa_or_wb == 0){
                    for (int i = 1;i<strArr.length;i++){
                        data_tmp[i] = Double.parseDouble(strArr[i]);
                    }
                    data_tmp[0] = 1;
                    data_xa.add(data_tmp);
                    data_y.add(Double.parseDouble(strArr[0]));
                }else{
                    for (int i = 1;i<strArr.length;i++){
                        data_tmp[i] = Double.parseDouble(strArr[i]);
                    }
                    data_tmp[0] = 1;
                    data_xb.add(data_tmp);
                }
                no += 1;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<double[]> SGD(ArrayList<double[]> train_xa,ArrayList<double[]> train_xb,ArrayList<Double> train_y
            ,double r,double c ,double a){
        double[] wa = new double[train_xa.get(0).length];
        double[] wb = new double[train_xb.get(0).length];
        int batch = train_xa.size()/5;
        for (int t = 0 ;t<times;t++){
            grad_xa = new double[wa.length];
            for (int i = 0;i<batch;i++){
                int randomNum = random.nextInt(train_xa.size());
                double[] i_xa = train_xa.get(randomNum);
                double[] i_xb = train_xb.get(randomNum);
                double i_y = train_y.get(randomNum);
                double cesi_xa = (i_y * Matrix.mulMatrix(i_xa, wa)) - 1;
                double cesi_xb = (i_y * Matrix.mulMatrix(i_xb, wb)) - 1;
                for (int k = 0;k<grad_xa.length;k++){
                    grad_xa[k] += a*i_y*i_xa[k]*(Math.exp(a*(cesi_xa+cesi_xb))-1);
                }
            }
            for (int k = 0;k<grad_xa.length;k++) grad_xa[k] = wa[k]+grad_xa[k]/batch;
            for (int k = 0;k<grad_xa.length;k++){
                wa[k] = wa[k]-grad_xa[k]*learn_rate;
            }

            grad_xb = new double[wb.length];
            for (int i = 0;i<batch;i++){
                int randomNum = random.nextInt(train_xb.size());
                double[] i_xa = train_xa.get(randomNum);
                double[] i_xb = train_xb.get(randomNum);
                double i_y = train_y.get(randomNum);
                double cesi_xa = (i_y * Matrix.mulMatrix(i_xa, wa)) - 1;
                double cesi_xb = (i_y * Matrix.mulMatrix(i_xb, wb)) - 1;
                for (int k = 0;k<grad_xb.length;k++){
                    grad_xb[k] += a*i_y*i_xb[k]*(Math.exp(a*(cesi_xa+cesi_xb))-1);
                }
            }
            for (int k = 0;k<grad_xb.length;k++) grad_xb[k] = r*wb[k]+c*grad_xb[k]/batch;
            for (int k = 0;k<grad_xb.length;k++){
                wb[k] = wb[k]-grad_xb[k]*learn_rate;
            }
//            if (t==times-1){
//                System.out.println("wa-"+t+":"+Arrays.toString(wa));
//                System.out.println("wb-"+t+":"+Arrays.toString(wb));
//            }
        }
        ArrayList<double[]> result = new ArrayList<>();
        result.add(wa);
        result.add(wb);
        return result;
    }
    public static double acc(ArrayList<double[]> test_xa ,ArrayList<double[]> test_xb,ArrayList<Double> test_y,ArrayList<double[]> w){
        double[] wa = w.get(0);
        double[] wb = w.get(1);
        double tp = 0;
        double fp = 0;
        double tn = 0;
        double fn = 0;
        for (int i = 0;i<test_xa.size();i++){
            double pre = Matrix.mulMatrix(test_xb.get(i),wb);
            if (pre>0){
                if (test_y.get(i) ==1){
                    tp += 1;
                }else{
                    fp += 1;
                }
            }else{
                if (test_y.get(i) == -1){
                    tn += 1;
                }else{
                    fn += 1;
                }
            }
        }
        tp_tol += tp;
        fp_tol += fp;
        tn_tol += tn;
        fn_tol += fn;
        double acc = (double) (tp+tn)/(fp+fn+tp+tn);
        //System.out.println(acc);
        return acc;
    }
}
