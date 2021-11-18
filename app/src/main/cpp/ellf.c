/* ellf.c
 *
 * Read ellf.doc before attempting to compile this program.
 */


#include <stdio.h>

/* System configurations */
#include "ellf.h"

static double wr = 0.0;
static double cbp = 0.0;
static double wc = 0.0;
static double rn = 8.0;
static double c = 0.0;
static double cgam = 0.0;
static double scale = 0.0;
double fs = 1.0e4;
static double dbr = 0.5;
static double dbd = -40.0;
static double f1 = 1.5e3;
static double f2 = 2.0e3;
static double f3 = 2.4e3;
double dbfac = 0.0;
static double a = 0.0;
static double b = 0.0;
static double q = 0.0;
static double r = 0.0;
static double u = 0.0;
static double k = 0.0;
static double m = 0.0;
static double Kk = 0.0;
static double Kk1 = 0.0;
static double Kpk = 0.0;
static double Kpk1 = 0.0;
static double eps = 0.0;
static double rho = 0.0;
static double phi = 0.0;
static double sn = 0.0;
static double cn = 0.0;
static double dn = 0.0;
static double sn1 = 0.0;
static double cn1 = 0.0;
static double dn1 = 0.0;
static double phi1 = 0.0;
static double m1 = 0.0;
static double m1p = 0.0;
static double cang = 0.0;
static double sang = 0.0;
static double bw = 0.0;
static double ang = 0.0;
double fnyq = 0.0;
static double ai = 0.0;
static double pn = 0.0;
static double an = 0.0;
static double gam = 0.0;
static double cng = 0.0;
double gain = 0.0;
static int lr = 0;
static int nt = 0;
static int i = 0;
static int j = 0;
static int jt = 0;
static int nc = 0;
static int ii = 0;
static int ir = 0;
int zord = 0;
static int icnt = 0;
static int mh = 0;
static int jj = 0;
static int jh = 0;
static int jl = 0;
static int n = 8;
static int np = 0;
static int nz = 0;
static int type = 1;
static int kind = 1;

static char wkind[] =
        { "Filter kind:\n1 Butterworth\n2 Chebyshev\n3 Elliptic\n" };

static char salut[] =
        { "Filter shape:\n1 low pass\n2 band pass\n3 high pass\n4 band stop\n" };

//int main() {
//
//
//	int result = butter_coeff(1, 1, 60.0f, 0.4f);
//
//	printf("%17.9E  %17.9E  %17.9E  %17.9E\n", aa[0], aa[1], pp[0], pp[1]);
//
//	if (result)
//		return 1;
//
//	return 0;
//}

/*
* This functions just returns the coefficients in an array of doubles
*/
int butter_coeff(int filt_type, int filt_order,
                 double f_sample, double f_cut)
{

    dbfac = 10.0/log(10.0);
    type = filt_type;
    kind = 1;
    n = filt_order;
    fs = f_sample;
    f2 = f_cut;

    if (n <= 0)
    {
        return 1;
    }
    rn = n;	/* ensure it is an integer */

    if (fs <= 0.0)
        return 1;

    fnyq = 0.5 * fs;

    if ((f2 <= 0.0) || (f2 >= fnyq))
        return 1;

    if ((type & 1) == 0)
    {
        if ((f1 <= 0.0) || (f1 >= fnyq))
            return 1;
    }
    else
    {
        f1 = 0.0;
    }

    if (f2 < f1)
    {
        a = f2;
        f2 = f1;
        f1 = a;
    }

    bw = f2 - f1;
    a = f2;

    ang = bw * PI / fs;
    cang = cos(ang);
    c = sin(ang) / cang; /* Wanalog */
    wc = c;

    a = PI * (a + f1) / fs;
    cgam = cos(a) / cang;
    a = 2.0 * PI * f2 / fs;
    cbp = (cgam - cos(a)) / sin(a);
    scale = 1.0;

    spln();		/* find s plane poles and zeros */

    if (((type & 1) == 0) && ((4 * n + 2) > ARRSIZ))
        return 1;

    zplna();	/* convert s plane to z plane */
    zplnb();
    gain = an / (pn * scale);
    if ((kind != 3) && (pn == 0))
        gain = 1.0;
    //printf("constant gain factor %23.13E\n", gain);
    for (j = 0; j <= zord; j++)
        pp[j] = gain * pp[j];

    /*printf("%17.9E %17.9E\n", pp[0], pp[1]);*/

    return 0;
}


/* calculate s plane poles and zeros, normalized to wc = 1 */
int spln()
{
    for( i=0; i<ARRSIZ; i++ )
        zs[i] = 0.0;
    np = (n+1)/2;
    nz = 0;
    if( kind == 1 )
    {
        /* Butterworth poles equally spaced around the unit circle
         */
        if( n & 1 )
            m = 0.0;
        else
            m = PI / (2.0*n);
        for( i=0; i<np; i++ )
        {	/* poles */
            lr = i + i;
            zs[lr] = -cos(m);
            zs[lr+1] = sin(m);
            m += PI / n;
        }
        /* high pass or band reject
         */
        if( type >= 3 )
        {
            /* map s => 1/s
             */
            for( j=0; j<np; j++ )
            {
                ir = j + j;
                ii = ir + 1;
                b = zs[ir]*zs[ir] + zs[ii]*zs[ii];
                zs[ir] = zs[ir] / b;
                zs[ii] = zs[ii] / b;
            }
            /* The zeros at infinity map to the origin.
             */
            nz = np;
            if( type == 4 )
            {
                nz += n/2;
            }
            for( j=0; j<nz; j++ )
            {
                ir = ii + 1;
                ii = ir + 1;
                zs[ir] = 0.0;
                zs[ii] = 0.0;
            }
        }
    }
    j = 0;
    for( i=0; i<np+nz; i++ )
    {
        a = zs[j];
        ++j;
        b = zs[j];
        ++j;
        //printf( "%.9E %.9E\n", a, b );
        //if( i == np-1 )
        //printf( "s plane zeros:\n" );
    }
    return 0;
}


/*		cay()
 *
 * Find parameter corresponding to given nome by expansion
 * in theta functions:
 * AMS55 #16.38.5, 16.38.7
 *
 *       1/2
 * ( 2K )                   4     9
 * ( -- )     =  1 + 2q + 2q  + 2q  + ...  =  Theta (0,q)
 * ( pi )                                          3
 *
 *
 *       1/2
 * ( 2K )     1/4       1/4        2    6    12    20
 * ( -- )    m     =  2q    ( 1 + q  + q  + q   + q   + ...) = Theta (0,q)
 * ( pi )                                                           2
 *
 * The nome q(m) = exp( - pi K(1-m)/K(m) ).
 *
 *                                1/2
 * Given q, this program returns m   .
 */
double cay(double q)
{
    double a, b, p, r;
    double t1, t2;

    a = 1.0;
    b = 1.0;
    r = 1.0;
    p = q;

    do
    {
        r *= p;
        a += 2.0 * r;
        t1 = fabs( r/a );

        r *= p;
        b += r;
        p *= q;
        t2 = fabs( r/b );
        if( t2 > t1 )
            t1 = t2;
    }
    while( t1 > MACHEP );

    a = b/a;
    a = 4.0 * sqrt(q) * a * a;	/* see above formulas, solved for m */
    return(a);
}


/*		zpln.c
 * Program to convert s plane poles and zeros to the z plane.
 */
int zplna()
{
    cmplx r, cnum, cden, cwc, ca, cb, b4ac;
    double C;

    if( kind == 3 )
        C = c;
    else
        C = wc;

    for( i=0; i<ARRSIZ; i++ )
    {
        z[i].r = 0.0;
        z[i].i = 0.0;
    }

    nc = np;
    jt = -1;
    ii = -1;

    for( icnt=0; icnt<2; icnt++ )
    {
        /* The maps from s plane to z plane */
        do
        {
            ir = ii + 1;
            ii = ir + 1;
            r.r = zs[ir];
            r.i = zs[ii];

            switch( type )
            {
                case 1:
                case 3:
                    /* Substitute  s - r  =  s/wc - r = (1/wc)(z-1)/(z+1) - r
                     *
                     *     1  1 - r wc (       1 + r wc )
                     * =  --- -------- ( z  -  -------- )
                     *    z+1    wc    (       1 - r wc )
                     *
                     * giving the root in the z plane.
                     */
                    cnum.r = 1 + C * r.r;
                    cnum.i = C * r.i;
                    cden.r = 1 - C * r.r;
                    cden.i = -C * r.i;
                    jt += 1;
                    cdiv( &cden, &cnum, &z[jt] );
                    if( r.i != 0.0 )
                    {
                        /* fill in complex conjugate root */
                        jt += 1;
                        z[jt].r = z[jt-1 ].r;
                        z[jt].i = -z[jt-1 ].i;
                    }
                    break;

                case 2:
                case 4:
                    /* Substitute  s - r  =>  s/wc - r
                     *
                     *     z^2 - 2 z cgam + 1
                     * =>  ------------------  -  r
                     *         (z^2 + 1) wc
                     *
                     *         1
                     * =  ------------  [ (1 - r wc) z^2  - 2 cgam z  +  1 + r wc ]
                     *    (z^2 + 1) wc
                     *
                     * and solve for the roots in the z plane.
                     */
                    if( kind == 2 )
                        cwc.r = cbp;
                    else
                        cwc.r = c;
                    cwc.i = 0.0;
                    cmul( &r, &cwc, &cnum );     /* r wc */
                    csub( &cnum, &cone, &ca );   /* a = 1 - r wc */
                    cmul( &cnum, &cnum, &b4ac ); /* 1 - (r wc)^2 */
                    csub( &b4ac, &cone, &b4ac );
                    b4ac.r *= 4.0;               /* 4ac */
                    b4ac.i *= 4.0;
                    cb.r = -2.0 * cgam;          /* b */
                    cb.i = 0.0;
                    cmul( &cb, &cb, &cnum );     /* b^2 */
                    csub( &b4ac, &cnum, &b4ac ); /* b^2 - 4 ac */
                    csqrt( &b4ac, &b4ac );
                    cb.r = -cb.r;  /* -b */
                    cb.i = -cb.i;
                    ca.r *= 2.0; /* 2a */
                    ca.i *= 2.0;
                    cadd( &b4ac, &cb, &cnum );   /* -b + sqrt( b^2 - 4ac) */
                    cdiv( &ca, &cnum, &cnum );   /* ... /2a */
                    jt += 1;
                    cmov( &cnum, &z[jt] );
                    if( cnum.i != 0.0 )
                    {
                        jt += 1;
                        z[jt].r = cnum.r;
                        z[jt].i = -cnum.i;
                    }
                    if( (r.i != 0.0) || (cnum.i == 0) )
                    {
                        csub( &b4ac, &cb, &cnum );  /* -b - sqrt( b^2 - 4ac) */
                        cdiv( &ca, &cnum, &cnum );  /* ... /2a */
                        jt += 1;
                        cmov( &cnum, &z[jt] );
                        if( cnum.i != 0.0 )
                        {
                            jt += 1;
                            z[jt].r = cnum.r;
                            z[jt].i = -cnum.i;
                        }
                    }
            } /* end switch */
        }
        while( --nc > 0 );

        if( icnt == 0 )
        {
            zord = jt+1;
            if( nz <= 0 )
            {
                if( kind != 3 )
                    return(0);
                else
                    break;
            }
        }
        nc = nz;
    } /* end for() loop */
    return 0;
}


int zplnb()
{
    cmplx lin[2];

    lin[1].r = 1.0;
    lin[1].i = 0.0;

    if( kind != 3 )
    { /* Butterworth or Chebyshev */
/* generate the remaining zeros */
        while( 2*zord - 1 > jt )
        {
            if( type != 3 )
            {
                //printf( "adding zero at Nyquist frequency\n" );
                jt += 1;
                z[jt].r = -1.0; /* zero at Nyquist frequency */
                z[jt].i = 0.0;
            }
            if( (type == 2) || (type == 3) )
            {
                //printf( "adding zero at 0 Hz\n" );
                jt += 1;
                z[jt].r = 1.0; /* zero at 0 Hz */
                z[jt].i = 0.0;
            }
        }
    }
    else
    { /* elliptic */
        while( 2*zord - 1 > jt )
        {
            jt += 1;
            z[jt].r = -1.0; /* zero at Nyquist frequency */
            z[jt].i = 0.0;
            if( (type == 2) || (type == 4) )
            {
                jt += 1;
                z[jt].r = 1.0; /* zero at 0 Hz */
                z[jt].i = 0.0;
            }
        }
    }
//printf( "order = %d\n", zord );

/* Expand the poles and zeros into numerator and
 * denominator polynomials
 */
    for( icnt=0; icnt<2; icnt++ )
    {
        for( j=0; j<ARRSIZ; j++ )
        {
            pp[j] = 0.0;
            y[j] = 0.0;
        }
        pp[0] = 1.0;
        for( j=0; j<zord; j++ )
        {
            jj = j;
            if( icnt )
                jj += zord;
            a = z[jj].r;
            b = z[jj].i;
            for( i=0; i<=j; i++ )
            {
                jh = j - i;
                pp[jh+1] = pp[jh+1] - a * pp[jh] + b * y[jh];
                y[jh+1] =  y[jh+1]  - b * pp[jh] - a * y[jh];
            }
        }
        if( icnt == 0 )
        {
            for( j=0; j<=zord; j++ )
                aa[j] = pp[j];
        }
    }
/* Scale factors of the pole and zero polynomials */
    a = 1.0;
    switch( type )
    {
        case 3:
            a = -1.0;

        case 1:
        case 4:

            pn = 1.0;
            an = 1.0;
            for( j=1; j<=zord; j++ )
            {
                pn = a * pn + pp[j];
                an = a * an + aa[j];
            }
            break;

        case 2:
            gam = PI/2.0 - asin( cgam );  /* = acos( cgam ) */
            mh = zord/2;
            pn = pp[mh];
            an = aa[mh];
            ai = 0.0;
            if( mh > ((zord/4)*2) )
            {
                ai = 1.0;
                pn = 0.0;
                an = 0.0;
            }
            for( j=1; j<=mh; j++ )
            {
                a = gam * j - ai * PI / 2.0;
                cng = cos(a);
                jh = mh + j;
                jl = mh - j;
                pn = pn + cng * (pp[jh] + (1.0 - 2.0 * ai) * pp[jl]);
                an = an + cng * (aa[jh] + (1.0 - 2.0 * ai) * aa[jl]);
            }
    }
    return 0;
}