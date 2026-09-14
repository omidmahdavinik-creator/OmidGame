package com.omid.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class GameView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val letters = listOf("ی","ز","د","ش","ن","ا")
    private val selected = mutableListOf<Int>()
    private var solved = false
    private var stars = 0
    private var pulse = 0f
    private var particles = mutableListOf<Particle>()
    private val bg = LinearGradient(0f,0f,0f,1800f, Color.rgb(7,22,32), Color.rgb(20,45,55), Shader.TileMode.CLAMP)

    init { isFocusable = true; startPulse() }
    private fun startPulse(){ ValueAnimator.ofFloat(0f,1f).apply { duration=1600; repeatCount=ValueAnimator.INFINITE; addUpdateListener{ pulse=it.animatedValue as Float; invalidate() }; start() } }

    override fun onDraw(c: Canvas) {
        super.onDraw(c); val w=width.toFloat(); val h=height.toFloat()
        p.shader=bg; c.drawRect(0f,0f,w,h,p); p.shader=null
        drawTop(c,w); drawQuestion(c,w); drawAnswer(c,w); drawWheel(c,w,h)
        if(solved) drawVictory(c,w,h)
    }
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int,align:Paint.Align=Paint.Align.CENTER){p.typeface=Typeface.create("sans",Typeface.BOLD);p.textSize=size;p.color=color;p.textAlign=align;p.style=Paint.Style.FILL;c.drawText(s,x,y,p)}
    private fun round(c:Canvas,l:Float,t:Float,r:Float,b:Float,rad:Float,color:Int,stroke:Int?=null){p.style=Paint.Style.FILL;p.color=color;c.drawRoundRect(l,t,r,b,rad,rad,p); if(stroke!=null){p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=stroke;c.drawRoundRect(l,t,r,b,rad,rad,p);p.style=Paint.Style.FILL}}
    private fun drawTop(c:Canvas,w:Float){ round(c,0f,0f,w,92f,0f,Color.rgb(5,13,21)); text(c,"امید",28f,43f,27f,Color.WHITE,Paint.Align.LEFT); text(c,"★ ۱۲ سطح",28f,72f,14f,Color.YELLOW,Paint.Align.LEFT); text(c,"🪙 ۳۴۵۰",w-250f,45f,18f,Color.WHITE); text(c,"💎 ۱۲۸",w-120f,45f,18f,Color.WHITE); text(c,"♥ ۵",w-25f,45f,18f,Color.WHITE,Paint.Align.RIGHT)}
    private fun drawQuestion(c:Canvas,w:Float){ round(c,30f,125f,w-30f,270f,24f,Color.rgb(11,28,39),Color.rgb(180,140,50)); text(c,"مرحله ۲۵",w/2,160f,22f,Color.WHITE); text(c,"نام یک شهر تاریخی ایران",w/2,205f,21f,Color.WHITE); text(c,"را با حروف پایین پیدا کن.",w/2,238f,17f,Color.LTGRAY) }
    private fun drawAnswer(c:Canvas,w:Float){ val ans=if(solved) "یزد" else selected.joinToString(""){letters[it]}; text(c,if(ans.isEmpty())"ـــــــ" else ans,w/2,330f,34f,if(solved)Color.YELLOW else Color.WHITE); if(solved) text(c,"✓ درست است!",w/2,370f,18f,Color.GREEN)}
    private fun drawWheel(c:Canvas,w:Float,h:Float){ val cx=w/2; val cy=h*0.63f; val rad=min(w*0.32f,150f); p.style=Paint.Style.STROKE;p.strokeWidth=5f;p.color=Color.rgb(25,150,220);c.drawCircle(cx,cy,rad+25f,p); p.style=Paint.Style.FILL
        for(i in letters.indices){val a=(-90+i*60)*Math.PI/180;val x=cx+cos(a).toFloat()*rad;val y=cy+sin(a).toFloat()*rad;val active=selected.contains(i);val col=if(active)Color.rgb(255,183,40) else Color.rgb(16,48,68);round(c,x-42,y-42,x+42,y+42,22f,col,if(active)Color.YELLOW else Color.rgb(60,140,190));text(c,letters[i],x,y+13,30f,Color.WHITE)}
        round(c,28f,h-170f,190f,h-110f,18f,Color.rgb(12,42,55));text(c,"💡 راهنما",109f,h-132f,18f,Color.WHITE)
        round(c,w-190f,h-170f,w-28f,h-110f,18f,Color.rgb(12,130,75));text(c,"بازگشت",w-109f,h-132f,18f,Color.WHITE)
    }
    private fun drawVictory(c:Canvas,w:Float,h:Float){ for(pt in particles){p.color=pt.color;p.alpha=(255*pt.life).toInt();c.drawCircle(pt.x,pt.y,pt.r,p)};p.alpha=255; text(c,"✦  عالی بود!  ✦",w/2,h-220f,30f,Color.YELLOW);round(c,w-230f,h-95f,w-25f,h-35f,20f,Color.rgb(20,180,85));text(c,"مرحله بعد  ▶",w-127f,h-57f,20f,Color.WHITE) }
    override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;val w=width.toFloat();val h=height.toFloat();val cx=w/2;val cy=h*.63f;val rad=min(w*.32f,150f);val dx=e.x-cx;val dy=e.y-cy;val d=hypot(dx.toDouble(),dy.toDouble()); if(!solved && d >= (rad-55f) && d <= (rad+55f)){var ang=Math.toDegrees(atan2(dy.toDouble(),dx.toDouble()))+90;if(ang<0)ang+=360;val i=((ang+30)/60).toInt()%6;if(!selected.contains(i)){selected.add(i); if(selected.size>=3 && selected.takeLast(3)==listOf(0,1,2)){solve()}};invalidate();return true}; if(solved && e.x>w-240 && e.y>h-115){selected.clear();solved=false;stars=0;particles.clear();invalidate()};return true}
    private fun solve(){solved=true;stars=3;repeat(36){val a=Math.random()*Math.PI*2;val speed=2+Math.random()*7;particles.add(Particle(width/2f,height*.42f,2f+(Math.random()*4).toFloat(),(cos(a)*speed).toFloat(),(sin(a)*speed).toFloat(),Color.YELLOW))};invalidate();ValueAnimator.ofFloat(0f,1f).apply{duration=1400;addUpdateListener{val t=it.animatedValue as Float;particles.forEach{pt->pt.x+=pt.vx;pt.y+=pt.vy;pt.life=1-t};invalidate()};start()}}
    data class Particle(var x:Float,var y:Float,var r:Float,var vx:Float,var vy:Float,var color:Int,var life:Float=1f)
}
