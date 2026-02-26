import type { SVGAttributes } from 'react'

const AuthBackgroundShape = (props: SVGAttributes<SVGElement>) => {
  return (
    <svg width='686' height='671' viewBox='0 0 686 671' fill='none' xmlns='http://www.w3.org/2000/svg' {...props}>
      <path
        d='M80 120C80 72 118 34 166 34H520C568 34 606 72 606 120V551C606 599 568 637 520 637H166C118 637 80 599 80 551V120Z'
        stroke='var(--primary, #2563eb)'
        strokeOpacity='0.25'
        strokeDasharray='8 8'
      />
      <path
        d='M161 105C197 69 255 70 291 106L576 401C612 437 611 495 575 531L526 580C490 616 432 615 396 579L111 284C75 248 76 190 112 154L161 105Z'
        fill='var(--primary, #2563eb)'
        fillOpacity='0.08'
      />
      <path
        d='M181 91C221 53 285 53 325 93L594 362C634 402 634 466 594 506L501 599C461 639 397 639 357 599L88 330C48 290 48 226 88 186L181 91Z'
        stroke='var(--primary, #2563eb)'
        strokeOpacity='0.2'
      />
    </svg>
  )
}

export default AuthBackgroundShape
