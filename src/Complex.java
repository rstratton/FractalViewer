import java.math.BigDecimal;


public class Complex {
	private double re;
	private double im;
	
	public Complex(double re, double im){
		this.re = re;
		this.im = im;
	}
	
	public double norm(){
		return Math.sqrt(re*re + im*im);
	}
	
	public static Complex add(Complex a, Complex b){
		return new Complex(a.re + b.re, a.im + b.im);
	}
	
	public static Complex mul(Complex a, Complex b){
		return new Complex(a.re*b.re - a.im*b.im, a.re*b.im + b.re*a.im);
	}
	
	public String toString(){
		return "(" + this.re + ", " + this.im + ")";
	}

}

/*
public class Complex {
	private BigDecimal re;
	private BigDecimal im;
	
	public Complex(BigDecimal re, BigDecimal im){
		this.re = re;
		this.im = im;
	}
	
	public BigDecimal norm(){
		return BigDecimal.pow(re*re + im*im, 0.5);
	}
	
	public static Complex add(Complex a, Complex b){
		return new Complex(a.re + b.re, a.im + b.im);
	}
	
	public static Complex mul(Complex a, Complex b){
		return new Complex(a.re*b.re - a.im*b.im, a.re*b.im + b.re*a.im);
	}
	
	public String toString(){
		return "(" + this.re + ", " + this.im + ")";
	}

}
*/