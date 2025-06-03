# Popover

Android PopupWindow 封装

## 如何集成

[JitPack主页](https://jitpack.io/#goweii/Popover)

### 添加JitPack仓库

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 添加依赖

```kotlin
dependencies {
    implementation("com.github.goweii:Popover:Tag")
}
```

## 如何使用

### 基本使用

会自动计算对齐方式：

- 垂直方向优先放在锚点下面，空间不足则放在锚点上面
- 水平方向优先与锚点起始边对齐，空间不足则与结尾边对齐

```kotlin
binding.btnShowPopover.setOnClickListener { v ->
    ShadowPopover(this).apply {
        val binding = PopoverMenusBinding.inflate(layoutInflater)
        contentView = binding.root
    }.show(v)
}
```

### 设置箭头

箭头位置会自动计算：

```kotlin
binding.btnShowPopover.setOnClickListener { v ->
    ShadowPopover(this).apply {
        val binding = PopoverMenusBinding.inflate(layoutInflater)
        contentView = binding.root

        arrowWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16F,
            resources.displayMetrics
        )

        arrowHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            12F,
            resources.displayMetrics
        )

        arrowCornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            2F,
            resources.displayMetrics
        )
    }.show(v)
}
```

### 手动指定对齐方式

```kotlin
binding.btnShowPopover.setOnClickListener { v ->
    ShadowPopover(this).apply {
        val b = PopoverMenusBinding.inflate(layoutInflater)
        contentView = b.root

        alignment = RelativeAlignment(
            vertical = RelativeAlignment.Vertical.Bellow(),
            horizontal = RelativeAlignment.Horizontal.AlignEnd(),
        )
    }.show(v)
}
```

### 设置与锚点的间距

```kotlin
binding.btnShowPopover.setOnClickListener { v ->
    ShadowPopover(this).apply {
        val b = PopoverMenusBinding.inflate(layoutInflater)
        contentView = b.root

        val padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8F,
            resources.displayMetrics
        ).toInt()

        alignment = AnchorMargin(
            alignment = RelativeAlignment(),
            margin = padding,
        )
    }.show(v)
}
```

### 设置与窗口的间距

```kotlin
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
            alignment = RelativeAlignment(),
            padding = padding,
        )
    }.show(v)
}
```

### 同时设置与锚点的间距和与窗口的间距

```kotlin
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
            alignment= AnchorMargin(
                alignment = RelativeAlignment(),
                margin = padding,
            ),
            padding = padding,
        )
    }.show(v)
}
```

### 自定义对齐方式

```kotlin
binding.btnShowPopover.setOnClickListener { v ->
    ShadowPopover(this).apply {
        val b = PopoverMenusBinding.inflate(layoutInflater)
        contentView = b.root

        alignment = object : Popover.Alignment {
            override fun compute(anchor: Popover.Anchor, target: Rect) {
                TODO("Not yet implemented")
            }
        }
    }.show(v)
}
```