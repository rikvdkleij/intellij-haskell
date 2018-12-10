{
module MixedStatefulStateless where

import Komeji.Satori
import Komeji.Koishi
}

tokens :-

@reimu { simple ReimuToken }

<state> {
  @alice { simple AliceToken }
}

{
Stop the time!
}
