package com.example.paintapp

import android.app.ActionBar
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs
import kotlin.properties.Delegates


private const val STROKE_WIDTH = 12f

class MainActivity : AppCompatActivity(), PaintView.PaintController {

    private lateinit var pencil: ImageView
    private lateinit var arrow: ImageView
    private lateinit var rectangle: ImageView
    private lateinit var circle: ImageView
    private lateinit var palette: ImageView


    private lateinit var popupWindow: PopupWindow

    private var selectedTool = TOOL.Pencil

    private lateinit var extraCanvas: Canvas

    private lateinit var extraBitmap: Bitmap


    private val rectPoints = ArrayList<Pair<Rect, Int>>()
    private val arrowPaths = ArrayList<Pair<Path, Int>>()
    private val circlePaths = ArrayList<Pair<Path, Int>>()

    private var backgroundColor by Delegates.notNull<Int>()
    private var drawColor by Delegates.notNull<Int>()

    //Setup the paint
    private lateinit var paint: Paint

    private val path = Path()
    private lateinit var arrowPath: Path
    private lateinit var circlePath: Path
    private var rect = Rect()
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f


    private var currentX = 0f
    private var currentY = 0f

    private var touchTolerance by Delegates.notNull<Int>()

    private lateinit var linearLayout: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pencil = findViewById(R.id.pencil)
//        pencil.background = ResourcesCompat.getDrawable(resources, R.drawable.tool_background, null)
        arrow = findViewById(R.id.arrow)
        rectangle = findViewById(R.id.rectangle)
        circle = findViewById(R.id.circle)
        palette = findViewById(R.id.colorPalatte)
        linearLayout = findViewById(R.id.linearLayout)
        backgroundColor =
            ResourcesCompat.getColor(resources, R.color.white, null)
        setToolBackground()
        drawColor = ResourcesCompat.getColor(resources, R.color.black, null)
        touchTolerance = ViewConfiguration.get(this).scaledTouchSlop
        paint = Paint().apply {
            color = drawColor
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
        }

        subscribeListener()
        findViewById<PaintView>(R.id.draw_view).setOnClickListener {
            if (::popupWindow.isInitialized && popupWindow.isShowing)
                popupWindow.dismiss()
        }



    }

    private fun setToolBackground() {
        clearBackground()
        when (selectedTool) {
            TOOL.Pencil -> {

                pencil.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.tool_background, null)
                pencil.setColorFilter(Color.BLACK)
            }
            TOOL.Arrow -> {

                arrow.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.tool_background, null)
                arrow.setColorFilter(Color.BLACK)
            }
            TOOL.Rectangle -> {

                rectangle.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.tool_background, null)
                rectangle.setColorFilter(Color.BLACK)
            }
            TOOL.Circle -> {
                circle.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.tool_background, null)
                circle.setColorFilter(Color.BLACK)
            }
            else -> {}
        }
    }

    private fun subscribeListener() {
        pencil.setOnClickListener {

            selectedTool = TOOL.Pencil
            setToolBackground()
        }
        arrow.setOnClickListener {

            selectedTool = TOOL.Arrow
            setToolBackground()
        }
        rectangle.setOnClickListener {

            selectedTool = TOOL.Rectangle
            setToolBackground()
        }
        circle.setOnClickListener {

            selectedTool = TOOL.Circle
            setToolBackground()
        }
        palette.setOnClickListener {
            clearBackground()
            if (::popupWindow.isInitialized && popupWindow.isShowing) {
                popupWindow.dismiss()
            } else {

                it.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.tool_background, null)
                palette.setColorFilter(Color.BLACK)
                val layoutInflater =
                    this@MainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val customView: View = layoutInflater.inflate(R.layout.color_layout, null)
                popupWindow =
                    PopupWindow(
                        customView,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT
                    )
                //display the popup window
                Log.d("MainActivity",palette.height.toString())

                popupWindow.showAsDropDown(palette, -palette.width*4,
                    (palette.height/2).toInt(),Gravity.END)

                customView.findViewById<View>(R.id.redColor)?.setOnClickListener {
                    drawColor = ResourcesCompat.getColor(resources, R.color.red, null)
                    popupWindow.dismiss()
                }
                customView.findViewById<View>(R.id.blueColor)?.setOnClickListener {
                    drawColor = ResourcesCompat.getColor(resources, R.color.blue, null)
                    popupWindow.dismiss()
                }
                customView.findViewById<View>(R.id.greenColor)?.setOnClickListener {
                    drawColor = ResourcesCompat.getColor(resources, R.color.green, null)
                    popupWindow.dismiss()
                }
                customView.findViewById<View>(R.id.blackColor)?.setOnClickListener {
                    drawColor = ResourcesCompat.getColor(resources, R.color.black, null)
                    popupWindow.dismiss()
                }
                popupWindow.setOnDismissListener {
                    setToolBackground()
                }
            }


        }

    }


    private fun clearBackground() {
        if(::popupWindow.isInitialized && popupWindow.isShowing){
            popupWindow.dismiss()
        }
        pencil.background = null
        pencil.setColorFilter(ResourcesCompat.getColor(resources,R.color.toolColorUnselected,null))
        arrow.background = null
        arrow.setColorFilter(ResourcesCompat.getColor(resources,R.color.toolColorUnselected,null))
        rectangle.background = null
        rectangle.setColorFilter(ResourcesCompat.getColor(resources,R.color.toolColorUnselected,null))
        circle.background = null
        circle.setColorFilter(ResourcesCompat.getColor(resources,R.color.toolColorUnselected,null))
        palette.background = null
        palette.setColorFilter(ResourcesCompat.getColor(resources,R.color.toolColorUnselected,null))
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)

        for (i in rectPoints) {
            canvas.drawRect(i.first, paint.apply { color = i.second })
        }
        for (path in arrowPaths) {
            canvas.drawPath(path.first, paint.apply { color = path.second })
        }
        for (circle in circlePaths) {
            canvas.drawPath(circle.first, paint.apply { color = circle.second })
        }
        when (selectedTool) {

            TOOL.Arrow -> {
                drawArrow(
                    canvas,
                    currentX,
                    currentY,
                    motionTouchEventX,
                    motionTouchEventY,
                    paint.apply { color = drawColor })
            }
            TOOL.Rectangle -> {

                canvas.drawRect(rect, paint.apply { color = drawColor })

            }
            TOOL.Circle -> {
                drawCircle(canvas)
            }
            else -> {

            }
        }

    }

    override fun touchMove(event: MotionEvent) {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {


            if (selectedTool == TOOL.Rectangle)
                rect = Rect(
                    currentX.toInt(), currentY.toInt(), motionTouchEventX.toInt(),
                    motionTouchEventY.toInt()
                )
            if (selectedTool == TOOL.Pencil) {
                // QuadTo() adds a quadratic bezier from the last point,
                // approaching control point (x1,y1), and ending at (x2,y2).
//            path.addArc(currentX, currentY,motionTouchEventX,motionTouchEventY,0f,0f)
                path.quadTo(
                    currentX,
                    currentY,
                    (motionTouchEventX + currentX) / 2,
                    (motionTouchEventY + currentY) / 2
                )
                extraCanvas.drawPath(path, paint.apply { color = drawColor })
                currentX = motionTouchEventX
                currentY = motionTouchEventY
            }
        }

    }

    override fun touchUp(event: MotionEvent) {
        if (selectedTool == TOOL.Rectangle)
            rectPoints.add(Pair(rect, drawColor))
        if (::arrowPath.isInitialized && selectedTool == TOOL.Arrow)
            arrowPaths.add(Pair(arrowPath, drawColor))
        if (selectedTool == TOOL.Pencil) {
            path.reset()
        }


        if (selectedTool == TOOL.Circle && ::circlePath.isInitialized) {
            circlePaths.add(Pair(circlePath, drawColor))
        }
    }

    override fun touchStart(event: MotionEvent) {
        path.reset()
        if (::arrowPath.isInitialized)
            arrowPath.reset()
        if (::circlePath.isInitialized)
            circlePath.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    override fun drawArrow(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        stopX: Float,
        stopY: Float,
        paint: Paint
    ) {
        val dx = stopX - startX
        val dy = stopY - startY
        val rad = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()

        arrowPath = Path()
        arrowPath.moveTo(currentX, currentY)
        arrowPath.lineTo(stopX, stopY)
        arrowPath.lineTo(
            //from   w w  w .ja v  a2 s.c om
            (stopX + Math.cos(rad + Math.PI * 0.75) * 40).toFloat(),
            (stopY + Math.sin(rad + Math.PI * 0.75) * 40).toFloat()
        )
        arrowPath.moveTo(stopX, stopY)
        arrowPath.lineTo(
            (stopX + Math.cos(rad - Math.PI * 0.75) * 40).toFloat(),
            (stopY + Math.sin(rad - Math.PI * 0.75) * 40).toFloat()
        )
        canvas.drawPath(arrowPath, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        Log.d("MainActivity", "called onSizeChanged")
        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }


    override fun setMotionTouchEvent(event: MotionEvent) {
        motionTouchEventX = event.x
        motionTouchEventY = event.y
    }

    private fun drawCircle(canvas: Canvas) {
        circlePath = Path()
        circlePath.moveTo(currentX, currentY)
        circlePath.addOval(
            currentX,
            currentY,
            motionTouchEventX,
            motionTouchEventY,
            Path.Direction.CW
        )
        canvas.drawPath(circlePath, paint.apply { color = drawColor })
    }

    override fun onPause() {
        if(::popupWindow.isInitialized && popupWindow.isShowing){
            popupWindow.dismiss()
        }
        super.onPause()
    }

}