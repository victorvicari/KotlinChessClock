package com.theopensourcefamily.chessclock

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.view.clicks
import com.theopensourcefamily.chessclock.extensions.centisecondsToHumanFriendlyTime
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_clocks.*
import javax.inject.Named

@Component(modules = [ClockPresenterFactory.MainModule::class])
interface ClockPresenterFactory {
  fun clockPresenter(): ClocksPresenter

  @Module
  class MainModule {
    @Provides
    @Named("mainScheduler")
    fun providesMainScheduler(): Scheduler = AndroidSchedulers.mainThread()
  }
}

class ClocksActivity : AppCompatActivity(), ClocksView {

  private val presenter = DaggerClockPresenterFactory.create().clockPresenter()

  override val userInteractions: Observable<ClocksView.Interaction>
    get() = Observable.merge(
      blackClock.clicks().map { ClocksView.Interaction.BlackPressed },
      whiteClock.clicks().map { ClocksView.Interaction.WhitePressed },
      pauseButton.clicks().map { ClocksView.Interaction.StopPressed },
      resetButton.clicks().map { ClocksView.Interaction.ResetPressed }
    )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_clocks)

    presenter.bindView(this)
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.unbind()
  }

  override fun render(state: ClockState) {
    when (state) {
      is ClockState.GameOver -> gameOverState(state)
      is ClockState.WhiteRunning, is ClockState.BlackRunning -> runningState()
      is ClockState.Stopped -> stoppedState(state)
    }
    whiteClock.text = state.whitesTime.centisecondsToHumanFriendlyTime()
    blackClock.text = state.blacksTime.centisecondsToHumanFriendlyTime()
  }

  private fun runningState() {
    pauseButton.visibility = VISIBLE
    resetButton.visibility = GONE
  }

  private fun stoppedState(state: ClockState.Stopped) {
    pauseButton.visibility = GONE
    whiteClock.setBackgroundColor(ContextCompat.getColor(this, R.color.ivory))
    blackClock.setBackgroundColor(ContextCompat.getColor(this, R.color.darkish))
    if (state.canReset) resetButton.visibility = VISIBLE else resetButton.visibility = GONE
  }

  private fun gameOverState(state: ClockState) {
    pauseButton.visibility = GONE
    resetButton.visibility = VISIBLE
    when {
      state.whitesTime == 0L -> whiteClock.setBackgroundColor(Color.RED)
      state.blacksTime == 0L -> blackClock.setBackgroundColor(Color.RED)
      else -> throw RuntimeException("One of the clocks should be at 0 time")
    }
    (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(200)
  }
}
