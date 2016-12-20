package com.sms1516.porcelli.daniele.wichat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * La classe permette di creare una riga di divisione per ogni Item che compare nella recyclerView.
 * Created by Giancosimo on 15/12/2016.
 */
public class LineItemDecoration extends RecyclerView.ItemDecoration{
    private static final float DEAFAULT_LINE_WIDTH = 2.0f;
    private final float mLineWidth;
    private final Paint mPaint;

    /**
     * Il costruttore specifica una larghezza della linea di divisione di Default.
     */
    public LineItemDecoration() {
        this(DEAFAULT_LINE_WIDTH);
    }

    /**
     * Costruttore che permette di specificare la larghezza della linea di divisione
     * @param lineWidth larghezza della linea di divisione desiderata
     */
    public LineItemDecoration(final float lineWidth) {
        this.mLineWidth = lineWidth;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(Color.GRAY);
    }

    // Il metodo permette la semplice aggiunta dello spazio in basso per la linea.
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {

        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, (int) Math.floor(mLineWidth));
    }

    // Il metodo ha la responsabilità di disegnare la "decorazione" nello spazio messo a
    // disposizione nel precedente metodo. Questo metodo disegna sopra la View a differenza
    // del metodo onDraw(...) che disegna sotto la View.
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        //Otteniamo un riferimento del LayouManager attraverso il metodo getLayoutManager()
        //della RecyclerView.
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        //ciclo per scandire tutti gli elementi presenti nella recyclerView, al fine di disegnare
        //la linea per ciascuno di essi.
        for(int i = 0; i < parent.getChildCount(); i++) {
            final View child = parent.getChildAt(i);
            c.drawLine(
                    layoutManager.getDecoratedLeft(child),
                    layoutManager.getDecoratedBottom(child),
                    layoutManager.getDecoratedRight(child),
                    layoutManager.getDecoratedBottom(child),
                    mPaint);
        }
    }
}
