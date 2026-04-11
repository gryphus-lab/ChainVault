/*
 * Copyright (c) 2026. Gryphus Lab
 */
import React from 'react'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CCarousel,
  CCarouselCaption,
  CCarouselItem,
  CCol,
  CRow,
} from '@coreui/react'
import { DocsComponents, DocsExample } from '../../../components'

import AngularImg from '../../../assets/images/angular.jpg'
import ReactImg from '../../../assets/images/react.jpg'
import VueImg from '../../../assets/images/vue.jpg'

/**
 * Placeholder slides used for light/dark carousel examples
 *
 * @type {string[]}
 */
const slidesLight = [
  'data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%22800%22%20height%3D%22400%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3Crect%20width%3D%22800%22%20height%3D%22400%22%20fill%3D%22%23f5f5f5%22%2F%3E%3Ctext%20x%3D%2250%25%22%20y%3D%2250%25%22%20dominant-baseline%3D%22middle%22%20text-anchor%3D%22middle%22%20font-family%3D%22monospace%22%20font-size%3D%2226px%22%20fill%3D%22%23999%22%3EFirst%20slide%3C%2Ftext%3E%3C%2Fsvg%3E',
  'data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%22800%22%20height%3D%22400%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3Crect%20width%3D%22800%22%20height%3D%22400%22%20fill%3D%22%23e5e5e5%22%2F%3E%3Ctext%20x%3D%2250%25%22%20y%3D%2250%25%22%20dominant-baseline%3D%22middle%22%20text-anchor%3D%22middle%22%20font-family%3D%22monospace%22%20font-size%3D%2226px%22%20fill%3D%22%23888%22%3ESecond%20slide%3C%2Ftext%3E%3C%2Fsvg%3E',
  'data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%22800%22%20height%3D%22400%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3Crect%20width%3D%22800%22%20height%3D%22400%22%20fill%3D%22%23d5d5d5%22%2F%3E%3Ctext%20x%3D%2250%25%22%20y%3D%2250%25%22%20dominant-baseline%3D%22middle%22%20text-anchor%3D%22middle%22%20font-family%3D%22monospace%22%20font-size%3D%2226px%22%20fill%3D%22%23777%22%3EThird%20slide%3C%2Ftext%3E%3C%2Fsvg%3E',
]

/**
 * Reusable slide images
 */
const frameworkImages = [
  { src: ReactImg, alt: 'React slide' },
  { src: AngularImg, alt: 'Angular slide' },
  { src: VueImg, alt: 'Vue slide' },
]

/**
 * Slide component for carousels
 */
const Slide = ({ src, alt }: { src: string; alt: string }) => (
  <CCarouselItem>
    <img className="d-block w-100" src={src} alt={alt} />
  </CCarouselItem>
)

const Carousels = () => {
  return (
    <CRow>
      {/* Documentation header */}
      <CCol xs={12}>
        <DocsComponents href="components/carousel/" />

        {/* Slide-only carousel */}
        <CCard className="mb-4">
          <CCardHeader>
            <strong>Carousel</strong> <small>Slide only</small>
          </CCardHeader>
          <CCardBody>
            <p className="text-body-secondary small">
              Basic carousel displaying slides without controls or indicators.
            </p>
            <DocsExample href="components/carousel">
              <CCarousel>
                {frameworkImages.map(({ src, alt }) => (
                  <Slide key={alt} src={src} alt={alt} />
                ))}
              </CCarousel>
            </DocsExample>
          </CCardBody>
        </CCard>
      </CCol>

      {/* Controls */}
      <CCol xs={12}>
        <CCard className="mb-4">
          <CCardHeader>
            <strong>Carousel</strong> <small>With controls</small>
          </CCardHeader>
          <CCardBody>
            <p className="text-body-secondary small">Adds previous/next navigation controls.</p>
            <DocsExample href="components/carousel/#with-controls">
              <CCarousel controls>
                {frameworkImages.map(({ src, alt }) => (
                  <Slide key={alt} src={src} alt={alt} />
                ))}
              </CCarousel>
            </DocsExample>
          </CCardBody>
        </CCard>
      </CCol>

      {/* Indicators */}
      <CCol xs={12}>
        <CCard className="mb-4">
          <CCardHeader>
            <strong>Carousel</strong> <small>With indicators</small>
          </CCardHeader>
          <CCardBody>
            <p className="text-body-secondary small">Adds slide indicators alongside controls.</p>
            <DocsExample href="components/carousel/#with-indicators">
              <CCarousel controls indicators>
                {frameworkImages.map(({ src, alt }) => (
                  <Slide key={alt} src={src} alt={alt} />
                ))}
              </CCarousel>
            </DocsExample>
          </CCardBody>
        </CCard>
      </CCol>

      {/* Captions */}
      <CCol xs={12}>
        <CCard className="mb-4">
          <CCardHeader>
            <strong>Carousel</strong> <small>With captions</small>
          </CCardHeader>
          <CCardBody>
            <p className="text-body-secondary small">
              Demonstrates captions with responsive visibility.
            </p>
            <DocsExample href="components/carousel/#with-captions">
              <CCarousel controls indicators>
                <CCarouselItem>
                  <img className="d-block w-100" src={ReactImg} alt="React slide" />
                  <CCarouselCaption className="d-none d-md-block">
                    <h5>React Framework</h5>
                    <p>A JavaScript library for building user interfaces.</p>
                  </CCarouselCaption>
                </CCarouselItem>
                <CCarouselItem>
                  <img className="d-block w-100" src={AngularImg} alt="Angular slide" />
                  <CCarouselCaption className="d-none d-md-block">
                    <h5>Angular Framework</h5>
                    <p>Platform for building mobile and desktop web applications.</p>
                  </CCarouselCaption>
                </CCarouselItem>
                <CCarouselItem>
                  <img className="d-block w-100" src={VueImg} alt="Vue slide" />
                  <CCarouselCaption className="d-none d-md-block">
                    <h5>Vue Framework</h5>
                    <p>The progressive JavaScript framework for building UIs.</p>
                  </CCarouselCaption>
                </CCarouselItem>
              </CCarousel>
            </DocsExample>
          </CCardBody>
        </CCard>
      </CCol>

      {/* Crossfade */}
      <CCol xs={12}>
        <CCard className="mb-4">
          <CCardHeader>
            <strong>Carousel</strong> <small>Crossfade</small>
          </CCardHeader>
          <CCardBody>
            <p className="text-body-secondary small">Uses fade transition instead of sliding.</p>
            <DocsExample href="components/carousel/#crossfade">
              <CCarousel controls transition="crossfade">
                {frameworkImages.slice(0, 2).map(({ src, alt }) => (
                  <Slide key={alt} src={src} alt={alt} />
                ))}
              </CCarousel>
            </DocsExample>
          </CCardBody>
        </CCard>
      </CCol>

      {/* Dark variant */}
      <CCol xs={12}>
        <CCard className="mb-4">
          <CCardHeader>
            <strong>Carousel</strong> <small>Dark variant</small>
          </CCardHeader>
          <CCardBody>
            <p className="text-body-secondary small">
              Demonstrates dark-themed carousel controls and captions.
            </p>
            <DocsExample href="components/carousel/#dark-variant">
              <CCarousel controls indicators dark>
                <CCarouselItem>
                  <img className="d-block w-100" src={slidesLight[0]} alt="First slide" />
                  <CCarouselCaption className="d-none d-md-block">
                    <h5>First slide label</h5>
                    <p>Some representative placeholder content for the first slide.</p>
                  </CCarouselCaption>
                </CCarouselItem>
                <CCarouselItem>
                  <img className="d-block w-100" src={slidesLight[1]} alt="Second slide" />
                  <CCarouselCaption className="d-none d-md-block">
                    <h5>Second slide label</h5>
                    <p>Some representative placeholder content for the second slide.</p>
                  </CCarouselCaption>
                </CCarouselItem>
                <CCarouselItem>
                  <img className="d-block w-100" src={slidesLight[2]} alt="Third slide" />
                  <CCarouselCaption className="d-none d-md-block">
                    <h5>Third slide label</h5>
                    <p>Some representative placeholder content for the third slide.</p>
                  </CCarouselCaption>
                </CCarouselItem>
              </CCarousel>
            </DocsExample>
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  )
}

export default Carousels