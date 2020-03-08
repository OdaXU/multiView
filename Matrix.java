public class Matrix {
    public static double mulMatrix(double[] i_x,double[] wa){
        //矩阵相乘
        double result = 0;
        for (int i = 0;i<i_x.length;i++) result += i_x[i] * wa[i];
        return result;
    }

    public static void main(String[] args){
        double[] a = new double[2];
        a[0] = 3;
        a[1] = 2;
        double[] b = new double[2];
        b[0] = 3;
        b[1] = 5;
        double val = Matrix.mulMatrix(a,b);
        System.out.println(val);
    }
}
