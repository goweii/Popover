package per.goweii.popover.simple

import android.os.Bundle
import android.util.TypedValue
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import per.goweii.popover.AnchorMargin
import per.goweii.popover.RelativeAlignment
import per.goweii.popover.WindowPadding
import per.goweii.popover.shadow.ShadowPopover
import per.goweii.popover.simple.databinding.ActivityMainBinding
import per.goweii.popover.simple.databinding.PopoverMenusBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.root.doOnPreDraw {
            binding.vsv.scrollTo(0, binding.vsv.maxScrollAmount / 2)
            binding.hsv.scrollTo(binding.hsv.maxScrollAmount / 2, 0)
        }

        binding.btnShowPopover.setOnClickListener { v ->
            ShadowPopover(this).apply {
                val b = PopoverMenusBinding.inflate(layoutInflater)
                contentView = b.root

                val padding = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8F,
                    resources.displayMetrics
                ).toInt()


                alignment = WindowPadding.all(
                    alignment = AnchorMargin(
                        alignment = RelativeAlignment(
                            vertical = RelativeAlignment.Vertical.Bellow(),
                            horizontal = RelativeAlignment.Horizontal.AlignEnd(),
                        ),
                        margin = padding,
                    ),
                    padding = padding,
                )
            }.show(v)
        }
    }
}