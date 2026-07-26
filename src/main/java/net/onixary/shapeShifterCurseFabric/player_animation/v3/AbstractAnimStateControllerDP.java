package net.onixary.shapeShifterCurseFabric.player_animation.v3;

import com.google.gson.JsonObject;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import org.jetbrains.annotations.Nullable;

/**
 * 从 JSON 数据包反序列化的动画状态控制器基类。
 * <p>
 * 数据包驱动的控制器在构造时从 {@link JsonObject} 读取配置，
 * 子类通过覆写 {@link #loadFormJson} 实现各自的初始化逻辑。
 * <p>
 * 提供无参构造器供 Java 代码直接实例化使用。
 *
 * @see AnimUtils#readController
 * @see net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimStateControllerDP
 */
public abstract class AbstractAnimStateControllerDP extends AbstractAnimStateController {
	/**
	 * 从 JSON 构造控制器。
	 *
	 * @param jsonData 控制器配置 JSON，为 null 时自动创建空对象并记录警告
	 */
    public AbstractAnimStateControllerDP(@Nullable JsonObject jsonData) {
        super();
        if (jsonData == null) {
            jsonData = new JsonObject();
            ShapeShifterCurseFabric.LOGGER.warn("jsonData is null");
        }
        loadFormJson(jsonData);
    }

	/**
	 * 无参构造器，供代码直接实例化。
	 */
    public AbstractAnimStateControllerDP() {
        super();
    }

	/**
	 * 从 JSON 对象加载控制器配置。
	 *
	 * @param jsonObject 控制器配置 JSON
	 * @return
	 */
	public abstract AbstractAnimStateController loadFormJson(JsonObject jsonObject);
}
